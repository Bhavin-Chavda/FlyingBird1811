package com.flyingbird.crypto.papertrading.service.impl;

import com.flyingbird.crypto.marketdata.model.Candle;
import com.flyingbird.crypto.papertrading.dto.CrossoverStateSnapshotDto;
import com.flyingbird.crypto.papertrading.dto.PatternDetectionResultDto;
import com.flyingbird.crypto.papertrading.entity.PaperCandle;
import com.flyingbird.crypto.papertrading.entity.PaperTrade;
import com.flyingbird.crypto.papertrading.enums.PaperTradeStatus;
import com.flyingbird.crypto.papertrading.repository.PaperCandleRepository;
import com.flyingbird.crypto.papertrading.repository.PaperTradeRepository;
import com.flyingbird.crypto.papertrading.service.CrossoverStateSnapshotService;
import com.flyingbird.crypto.papertrading.service.PaperTradeCreationService;
import com.flyingbird.crypto.papertrading.util.CandleTimeUtils;
import com.flyingbird.crypto.scheduler.common.Timeframe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts {@link PatternDetectionResultDto}s into persisted OPEN {@link PaperTrade}s.
 * The signal candle is stored once per (timeframe, candleTime) and reused for all
 * patterns on that candle.
 *
 * <p>Two duplicate guards, both inside the creation transaction:</p>
 * <ol>
 *   <li><b>Active-pattern guard</b> — skip if an <b>OPEN</b> trade already exists for the
 *       same {@code (timeframe, patternName, tradeType)}. A still-visible pattern
 *       re-detected on later candles will not spawn another trade until the existing one
 *       closes. Opposite direction, other patterns and other timeframes are unaffected.</li>
 *   <li><b>Same-candle guard</b> — skip an identical trade for the exact signal candle
 *       (re-run / restart), backed by the DB unique constraint.</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaperTradeCreationServiceImpl implements PaperTradeCreationService {

    private final PaperCandleRepository paperCandleRepository;
    private final PaperTradeRepository paperTradeRepository;
    private final CrossoverStateSnapshotService crossoverStateSnapshotService;

    @Override
    @Transactional
    public void createFromDetections(Timeframe timeframe, Candle signalCandle,
                                     List<PatternDetectionResultDto> results) {
        if (results == null || results.isEmpty() || signalCandle == null) {
            return;
        }
        LocalDateTime candleTime = CandleTimeUtils.parse(signalCandle.getTime());
        if (candleTime == null) {
            log.warn("[paper-trading] cannot parse candle time '{}' for {} — skipping persistence",
                    signalCandle.getTime(), timeframe.getCode());
            return;
        }

        // Decide which detections to persist BEFORE touching PaperCandle, so a batch that
        // is entirely duplicate creates no PaperCandle row (Edge Case G).
        List<PatternDetectionResultDto> toCreate = new ArrayList<>();
        for (PatternDetectionResultDto r : results) {
            // Guard 1 (active pattern): an OPEN trade for this timeframe+pattern+tradeType already exists.
            if (paperTradeRepository.existsByTimeframeAndPatternNameAndTradeTypeAndTradeStatus(
                    timeframe, r.getPatternName(), r.getTradeDirection(), PaperTradeStatus.OPEN)) {
                log.info("[paper-trading] Skipped duplicate paper trade creation: active OPEN trade already exists "
                                + "for timeframe={}, pattern={}, tradeType={}",
                        timeframe.getCode(), r.getPatternName(), r.getTradeDirection());
                continue;
            }
            // Guard 2 (same candle): identical trade already created for this exact signal candle.
            if (paperTradeRepository.existsByTimeframeAndPatternNameAndTradeTypeAndCandleTime(
                    timeframe, r.getPatternName(), r.getTradeDirection(), candleTime)) {
                log.debug("[paper-trading] duplicate skipped (same candle) | {} {} {} @ {}",
                        timeframe.getCode(), r.getPatternName(), r.getTradeDirection(), candleTime);
                continue;
            }
            toCreate.add(r);
        }
        if (toCreate.isEmpty()) {
            return;
        }

        PaperCandle candle = paperCandleRepository.findByTimeframeAndCandleTime(timeframe, candleTime)
                .orElseGet(() -> paperCandleRepository.save(toPaperCandle(timeframe, signalCandle, candleTime)));

        // Snapshot the latest 1m/5m/15m/1h crossover states ONCE for this batch (cheap, in-memory).
        // Captured only when at least one trade will be created (skipped batches snapshot nothing).
        CrossoverStateSnapshotDto crossover = crossoverStateSnapshotService.capture();

        for (PatternDetectionResultDto r : toCreate) {
            try {
                PaperTrade trade = toPaperTrade(timeframe, r, candle, candleTime, crossover);
                paperTradeRepository.save(trade);
                log.info("[paper-trading] trade created | {} {} {} entry={} sl={} tp1={} conf={}",
                        timeframe.getCode(), r.getTradeDirection(), r.getPatternName(),
                        r.getEntryPrice(), r.getStopLoss(), r.getTp1(), r.getConfidenceScore());
            } catch (DataIntegrityViolationException dup) {
                log.debug("[paper-trading] duplicate skipped (unique constraint) | {} {} {} @ {}",
                        timeframe.getCode(), r.getPatternName(), r.getTradeDirection(), candleTime);
            }
        }
    }

    private PaperCandle toPaperCandle(Timeframe timeframe, Candle c, LocalDateTime candleTime) {
        return PaperCandle.builder()
                .timeframe(timeframe)
                .candleTime(candleTime)
                .openPrice(bd(c.getOpen()))
                .highPrice(bd(c.getHigh()))
                .lowPrice(bd(c.getLow()))
                .closePrice(bd(c.getClose()))
                .volume(bd(c.getVolume()))
                .build();
    }

    private PaperTrade toPaperTrade(Timeframe timeframe, PatternDetectionResultDto r,
                                    PaperCandle candle, LocalDateTime candleTime,
                                    CrossoverStateSnapshotDto crossover) {
        LocalDateTime now = LocalDateTime.now();
        return PaperTrade.builder()
                .timeframe(timeframe)
                .tradeType(r.getTradeDirection())
                .tradeStatus(PaperTradeStatus.OPEN)
                .patternName(r.getPatternName())
                .candleTime(candleTime)
                .paperCandle(candle)
                .oneMinuteCrossoverState(crossover.getOneMinute())
                .fiveMinuteCrossoverState(crossover.getFiveMinute())
                .fifteenMinuteCrossoverState(crossover.getFifteenMinute())
                .oneHourCrossoverState(crossover.getOneHour())
                .tradePrice(r.getEntryPrice())
                .stopLoss(r.getStopLoss())
                .initialStopLoss(r.getStopLoss())
                .tp1(r.getTp1())
                .tp2(r.getTp2())
                .tp3(r.getTp3())
                .tp4(r.getTp4())
                .safeTrade(false)
                .tp1Achieved(false)
                .tp2Achieved(false)
                .tp3Achieved(false)
                .tp4Achieved(false)
                .openedAt(now)
                .lastEvaluatedAt(now)
                .confidenceScore(r.getConfidenceScore())
                .detectionReason(r.getDetectionReason())
                .patternStartTime(r.getPatternStartTime())
                .patternEndTime(r.getPatternEndTime())
                .breakoutLevel(r.getBreakoutLevel())
                .riskAmount(r.getRiskPerUnit())
                .atrAtDetection(r.getAtrAtDetection())
                .createdByVersion("v1")
                .build();
    }

    private static BigDecimal bd(double v) {
        return BigDecimal.valueOf(v);
    }
}
