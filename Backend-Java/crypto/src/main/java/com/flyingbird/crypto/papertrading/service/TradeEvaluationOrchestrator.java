package com.flyingbird.crypto.papertrading.service;

import com.flyingbird.crypto.marketdata.model.Candle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Serializes paper-trade evaluation onto a single-thread executor so evaluations
 * never overlap or run in parallel. Deduplicates by candle time so the same 1m
 * candle is not evaluated twice. Submission is non-blocking for the scheduler thread.
 */
@Slf4j
@Service
public class TradeEvaluationOrchestrator {

    private final TradeEvaluator tradeEvaluator;
    private final ThreadPoolTaskExecutor executor;
    private final AtomicReference<String> lastSubmittedCandleTime = new AtomicReference<>();

    public TradeEvaluationOrchestrator(TradeEvaluator tradeEvaluator,
                                       @Qualifier("paperTradeEvaluatorExecutor") ThreadPoolTaskExecutor executor) {
        this.tradeEvaluator = tradeEvaluator;
        this.executor = executor;
    }

    public void onNewOneMinuteCandle(Candle latestOneMinuteCandle) {
        if (latestOneMinuteCandle == null) {
            return;
        }
        String candleTime = latestOneMinuteCandle.getTime();
        String prev = lastSubmittedCandleTime.getAndSet(candleTime);
        if (candleTime != null && candleTime.equals(prev)) {
            log.debug("[paper-trading] 1m candle {} already submitted for evaluation — skipping", candleTime);
            return;
        }
        try {
            executor.execute(() -> {
                try {
                    tradeEvaluator.evaluateOpenTrades(latestOneMinuteCandle);
                } catch (Exception e) {
                    log.error("[paper-trading] trade evaluation failed for candle {}: {}",
                            candleTime, e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            // e.g. RejectedExecutionException — log and move on; next candle re-triggers.
            log.warn("[paper-trading] could not submit evaluation for candle {}: {}", candleTime, e.getMessage());
        }
    }
}
