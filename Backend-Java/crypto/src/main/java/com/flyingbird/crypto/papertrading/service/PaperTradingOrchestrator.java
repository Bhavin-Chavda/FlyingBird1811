package com.flyingbird.crypto.papertrading.service;

import com.flyingbird.crypto.marketdata.model.Candle;
import com.flyingbird.crypto.scheduler.common.Timeframe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Single entry point the per-job schedulers call after appending a new closed
 * candle. Fans out to (a) pattern detection for that timeframe (flag-gated, async)
 * and (b) — for the 1m timeframe only — paper-trade evaluation of all OPEN trades
 * (which runs regardless of detection flags). Fully isolated: any failure here is
 * logged and swallowed so it can never break a scheduler run.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaperTradingOrchestrator {

    private final PatternDetectionOrchestrator patternDetectionOrchestrator;
    private final TradeEvaluationOrchestrator tradeEvaluationOrchestrator;

    public void onNewClosedCandle(Timeframe timeframe, List<Candle> snapshot) {
        if (timeframe == null || snapshot == null || snapshot.isEmpty()) {
            return;
        }
        try {
            List<Candle> candles = List.copyOf(snapshot);
            // 1) Pattern detection (only if enabled for this timeframe).
            patternDetectionOrchestrator.onNewClosedCandle(timeframe, candles);
            // 2) Evaluate OPEN trades on each new 1m candle — independent of detection flags.
            if (timeframe == Timeframe.ONE_MINUTE) {
                tradeEvaluationOrchestrator.onNewOneMinuteCandle(candles.get(candles.size() - 1));
            }
        } catch (Exception e) {
            log.error("[paper-trading] hook failed for {}: {}", timeframe.getCode(), e.getMessage(), e);
        }
    }
}
