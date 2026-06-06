package com.flyingbird.crypto.scheduler.oneMinuteCandle;

import com.flyingbird.crypto.config.MarketDataProperties;
import com.flyingbird.crypto.config.NotificationProperties;
import com.flyingbird.crypto.papertrading.config.PaperTradingProperties;
import com.flyingbird.crypto.papertrading.dto.PatternDetectionResultDto;
import com.flyingbird.crypto.papertrading.enums.ChartPatternName;
import com.flyingbird.crypto.papertrading.enums.TradeDirection;
import com.flyingbird.crypto.papertrading.service.PaperTradeCreationService;
import com.flyingbird.crypto.papertrading.service.PaperTradingOrchestrator;
import com.flyingbird.crypto.papertrading.util.CandleTimeUtils;
import com.flyingbird.crypto.scheduler.common.Timeframe;
import com.flyingbird.crypto.marketdata.client.DeltaCandleClient;
import com.flyingbird.crypto.marketdata.model.Candle;
import com.flyingbird.crypto.scheduler.common.CandleCalculationUtils;
import com.flyingbird.crypto.scheduler.common.CrossoverStateDto;
import com.flyingbird.crypto.scheduler.common.SchedulerConstants;
import com.flyingbird.crypto.scheduler.common.SchedulerTimeUtils;
import com.flyingbird.crypto.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

/**
 * 1-minute candle service implementation (1m job business logic only).
 * Independent orchestration — shares only the stateless calculation utils.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OneMinuteCandleServiceImpl implements OneMinuteCandleService {

    private static final String RESOLUTION = "1m";
    private static final int BUCKET_SECONDS = 60;
    // DTC paper-trade plan (per-job; duplicated by design — each scheduler runs on its own thread).
    private static final int DTC_SL_LOOKBACK = 10;
    private static final int DTC_SCALE = 8;

    private final DeltaCandleClient deltaCandleClient;
    private final OneMinuteCandleStore store;
    private final MailService mailService;
    private final MarketDataProperties props;
    private final NotificationProperties notificationProps;
    private final PaperTradingOrchestrator paperTradingOrchestrator;
    private final PaperTradeCreationService paperTradeCreationService;
    private final PaperTradingProperties paperTradingProperties;

    @Override
    public void seed() {
        int maxPeriod = Arrays.stream(CandleCalculationUtils.EMA_PERIODS).max().orElse(200);
        int seedCount = props.getBufferLength() + maxPeriod * props.getSeedMultiple();
        List<Candle> history = deltaCandleClient.fetchLastNCandles(RESOLUTION, BUCKET_SECONDS, seedCount);
        CandleCalculationUtils.seedAndFill(history);
        store.seedReplace(history);
        recordInitialCrossover();
        log.info("[{}] 1m buffer seeded | size={}", Thread.currentThread().getName(), store.size());
    }

    @Override
    public int run() {
        if (store.size() == 0) {
            seed();
            return store.size();
        }

        Candle latest;
        try {
            latest = deltaCandleClient.getLatestCandle(RESOLUTION, BUCKET_SECONDS);
        } catch (Exception e) {
            log.warn("[{}] 1m fetch failed, refilling buffer: {}", Thread.currentThread().getName(), e.getMessage());
            seed();
            return store.size();
        }

        boolean added = store.appendLatest(latest);
        if (!added) {
            return store.size();
        }

        List<Candle> snap = store.snapshot();
        String signal = CandleCalculationUtils.evaluateSignal(snap);
        boolean transition = store.recordEvaluation(signal, latest.getTime(), SchedulerTimeUtils.nowIstLabel());
        if (transition && snap.size() >= 3) {
            emitSignal(signal, snap);
        }
        // Paper-trading hook: pattern detection (if enabled) + evaluate OPEN trades (1m only). Never breaks the job.
        paperTradingOrchestrator.onNewClosedCandle(Timeframe.ONE_MINUTE, snap);
        return store.size();
    }

    private void emitSignal(String signal, List<Candle> snap) {
        Candle last = snap.get(snap.size() - 1);

        // Paper-trading: build the DTC trade plan and (if enabled) create the paper trade.
        // Never breaks the scheduler/email.
        PatternDetectionResultDto result;
        try {
            result = createDtcPaperTrade(signal, snap);
        } catch (Exception e) {
            log.error("[paper-trading] DTC paper trade creation failed (1m): {}", e.getMessage(), e);
            result = null;
        }

        // Notification — the DTC trade plan is the email payload (replaces the old OrderRequest).
        String subject = (SchedulerConstants.SIGNAL_BULLISH.equals(signal) ? "BUY" : "SELL")
                + " SIGNAL GENERATED (1m)";
        String details = signal + " SIGNAL GENERATED (1m):\n" + SchedulerTimeUtils.nowIstLabel();
        if (notificationProps.isEmailEnabledFor(RESOLUTION)) {
            mailService.sendSignalEmail(last, result, subject, details);
        } else {
            log.info("Signal email disabled for timeframe {}. Signal generated but email was not sent.", RESOLUTION);
        }
    }

    /**
     * Create a DTC_INDICATOR paper trade from this crossover signal (1m). Flag-gated by
     * {@code paper-trading.dtc-indicator.enabled.one-minute}. Entry = latest close;
     * BULLISH SL = min low / BEARISH SL = max high of the last 10 candles; TP1-4 = 1R..4R.
     * Delegates to the shared creation flow (dedup + PaperCandle + crossover snapshot + save).
     * Kept inline per scheduler (no shared DTC service) since each job runs on its own thread.
     */
    private PatternDetectionResultDto createDtcPaperTrade(String signal, List<Candle> snap) {
        if (snap == null || snap.isEmpty()) {
            log.debug("[paper-trading] DTC skipped (1m): no candles");
            return null;
        }
        TradeDirection direction = dtcDirection(signal);
        if (direction == null) {
            log.debug("[paper-trading] DTC skipped (1m): non-directional signal '{}'", signal);
            return null;
        }

        Candle latest = snap.get(snap.size() - 1);
        List<Candle> lookback = snap.subList(Math.max(0, snap.size() - DTC_SL_LOOKBACK), snap.size());

        BigDecimal entry = dtcBd(latest.getClose());
        boolean bull = direction == TradeDirection.BULLISH;
        BigDecimal stopLoss = bull ? dtcMinLow(lookback) : dtcMaxHigh(lookback);
        BigDecimal risk = bull ? entry.subtract(stopLoss) : stopLoss.subtract(entry);

        if (risk.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("[paper-trading] DTC trade skipped (1m {}): non-positive risk (entry={}, sl={}, risk={})",
                    direction, entry, stopLoss, risk);
            return null;
        }

        PatternDetectionResultDto result = PatternDetectionResultDto.builder()
                .detected(true)
                .timeframe(Timeframe.ONE_MINUTE)
                .patternName(ChartPatternName.DTC_INDICATOR)
                .tradeDirection(direction)
                .signalCandleTime(CandleTimeUtils.parse(latest.getTime()))
                .entryPrice(entry)
                .stopLoss(stopLoss)
                .tp1(dtcTakeProfit(entry, risk, 1, bull))
                .tp2(dtcTakeProfit(entry, risk, 2, bull))
                .tp3(dtcTakeProfit(entry, risk, 3, bull))
                .tp4(dtcTakeProfit(entry, risk, 4, bull))
                .riskPerUnit(risk)
                .detectionReason("DTC crossover " + signal + " | entry=" + entry + " sl=" + stopLoss
                        + " (min/max of last " + lookback.size() + " candles)")
                .candlesAnalyzed(lookback.size())
                .build();

        // Persist only when DTC is enabled for this timeframe; the plan is returned either way
        // so the signal email can use it.
        if (paperTradingProperties.isDtcEnabled(Timeframe.ONE_MINUTE)) {
            paperTradeCreationService.createFromDetections(Timeframe.ONE_MINUTE, latest, List.of(result));
        } else {
            log.debug("[paper-trading] DTC persistence disabled (1m) — plan computed for signal email only");
        }

        return result;
    }

    private TradeDirection dtcDirection(String signal) {
        if (SchedulerConstants.SIGNAL_BULLISH.equals(signal)) {
            return TradeDirection.BULLISH;
        }
        if (SchedulerConstants.SIGNAL_BEARISH.equals(signal)) {
            return TradeDirection.BEARISH;
        }
        return null;
    }

    private BigDecimal dtcMinLow(List<Candle> candles) {
        BigDecimal min = dtcBd(candles.get(0).getLow());
        for (Candle c : candles) {
            min = min.min(dtcBd(c.getLow()));
        }
        return min;
    }

    private BigDecimal dtcMaxHigh(List<Candle> candles) {
        BigDecimal max = dtcBd(candles.get(0).getHigh());
        for (Candle c : candles) {
            max = max.max(dtcBd(c.getHigh()));
        }
        return max;
    }

    private BigDecimal dtcTakeProfit(BigDecimal entry, BigDecimal risk, int multiple, boolean bull) {
        BigDecimal move = risk.multiply(BigDecimal.valueOf(multiple));
        return (bull ? entry.add(move) : entry.subtract(move)).setScale(DTC_SCALE, RoundingMode.HALF_UP);
    }

    private static BigDecimal dtcBd(double v) {
        return BigDecimal.valueOf(v).setScale(DTC_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Record an initial crossover snapshot right after seeding so the state is
     * populated before the first scheduled run (no signal email on seed).
     */
    private void recordInitialCrossover() {
        List<Candle> seeded = store.snapshot();
        String signal = CandleCalculationUtils.evaluateSignal(seeded);
        String candleTime = seeded.isEmpty() ? null : seeded.get(seeded.size() - 1).getTime();
        store.recordEvaluation(signal, candleTime, SchedulerTimeUtils.nowIstLabel());
    }

    @Override
    public int bufferSize() {
        return store.size();
    }

    @Override
    public List<Candle> getBufferSnapshot() {
        return store.snapshot();
    }

    @Override
    public Candle getLastCandle() {
        return store.lastCandle();
    }

    @Override
    public CrossoverStateDto getCrossoverState() {
        return store.crossoverSnapshot();
    }
}
