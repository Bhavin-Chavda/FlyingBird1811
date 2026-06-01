package com.flyingbird.crypto.scheduler.oneMinuteCandle;

import com.flyingbird.crypto.config.MarketDataProperties;
import com.flyingbird.crypto.marketdata.client.DeltaCandleClient;
import com.flyingbird.crypto.marketdata.model.Candle;
import com.flyingbird.crypto.marketdata.model.OrderRequest;
import com.flyingbird.crypto.scheduler.common.CandleCalculationUtils;
import com.flyingbird.crypto.scheduler.common.CrossoverStateDto;
import com.flyingbird.crypto.scheduler.common.SchedulerConstants;
import com.flyingbird.crypto.scheduler.common.SchedulerTimeUtils;
import com.flyingbird.crypto.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    private final DeltaCandleClient deltaCandleClient;
    private final OneMinuteCandleStore store;
    private final MailService mailService;
    private final MarketDataProperties props;

    @Override
    public void seed() {
        int maxPeriod = Arrays.stream(CandleCalculationUtils.EMA_PERIODS).max().orElse(200);
        int seedCount = props.getBufferLength() + maxPeriod * props.getSeedMultiple();
        List<Candle> history = deltaCandleClient.fetchLastNCandles(RESOLUTION, BUCKET_SECONDS, seedCount);
        CandleCalculationUtils.seedAndFill(history);
        store.seedReplace(history);
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
        return store.size();
    }

    private void emitSignal(String signal, List<Candle> snap) {
        Candle last = snap.get(snap.size() - 1);
        Candle prev = snap.get(snap.size() - 2);
        Candle prevPrev = snap.get(snap.size() - 3);
        double entry = last.getClose();

        OrderRequest order;
        String subject;
        if (SchedulerConstants.SIGNAL_BULLISH.equals(signal)) {
            double sl = CandleCalculationUtils.min3(last.getLow(), prev.getLow(), prevPrev.getLow());
            double tp = entry + Math.abs(entry - sl) * 3;
            order = CandleCalculationUtils.buildOrder(RESOLUTION, props.getProductId(), "buy", sl, tp);
            subject = "BUY SIGNAL GENERATED (1m)";
        } else {
            double sl = CandleCalculationUtils.max3(last.getHigh(), prev.getHigh(), prevPrev.getHigh());
            double tp = entry - Math.abs(sl - entry) * 3;
            order = CandleCalculationUtils.buildOrder(RESOLUTION, props.getProductId(), "sell", sl, tp);
            subject = "SELL SIGNAL GENERATED (1m)";
        }
        String details = signal + " SIGNAL GENERATED (1m):\n" + SchedulerTimeUtils.nowIstLabel();
        mailService.sendSignalEmail(last, order, subject, details);
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
