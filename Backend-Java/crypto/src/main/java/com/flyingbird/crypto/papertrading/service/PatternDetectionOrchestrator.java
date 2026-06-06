package com.flyingbird.crypto.papertrading.service;

import com.flyingbird.crypto.marketdata.model.Candle;
import com.flyingbird.crypto.papertrading.config.PaperTradingProperties;
import com.flyingbird.crypto.papertrading.event.PatternDetectionRequestedEvent;
import com.flyingbird.crypto.scheduler.common.Timeframe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Decides whether/how to run pattern detection for a new closed candle: checks the
 * per-timeframe flag, takes an immutable snapshot, then either publishes an async
 * event (default) or processes synchronously. Never throws to the caller.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PatternDetectionOrchestrator {

    private static final int MIN_CANDLES = 40;

    private final PaperTradingProperties props;
    private final ApplicationEventPublisher eventPublisher;
    private final PatternDetectionProcessor processor;

    public void onNewClosedCandle(Timeframe timeframe, List<Candle> snapshot) {
        if (!props.isDetectionEnabled(timeframe)) {
            log.debug("[paper-trading] detection disabled for {} — skipping", timeframe.getCode());
            return;
        }
        if (snapshot == null || snapshot.size() < MIN_CANDLES) {
            log.debug("[paper-trading] not enough candles for {} ({}) — skipping",
                    timeframe.getCode(), snapshot == null ? 0 : snapshot.size());
            return;
        }
        List<Candle> candles = List.copyOf(snapshot); // immutable defensive copy
        Candle latest = candles.get(candles.size() - 1);

        try {
            if (props.getPatternDetector().isAsyncEnabled()) {
                eventPublisher.publishEvent(new PatternDetectionRequestedEvent(
                        timeframe, candles, latest.getTime(), latest.getClose(),
                        UUID.randomUUID().toString(), LocalDateTime.now()));
            } else {
                processor.process(timeframe, candles);
            }
        } catch (Exception e) {
            log.error("[paper-trading] failed to dispatch detection for {}: {}",
                    timeframe.getCode(), e.getMessage(), e);
        }
    }
}
