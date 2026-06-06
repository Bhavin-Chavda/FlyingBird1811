package com.flyingbird.crypto.papertrading.event;

import com.flyingbird.crypto.papertrading.service.PatternDetectionProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Async listener for {@link PatternDetectionRequestedEvent}. Runs on the bounded
 * {@code patternDetectorExecutor} so several timeframe events (e.g. 1m+5m+15m at
 * minute 15) are processed concurrently — none are skipped. Idempotency is enforced
 * downstream by the dedup check + unique constraint.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PatternDetectionEventListener {

    private final PatternDetectionProcessor processor;

    @Async("patternDetectorExecutor")
    @EventListener
    public void handlePatternDetectionRequested(PatternDetectionRequestedEvent event) {
        log.info("[paper-trading] detection request received | {} candle={} reqId={}",
                event.timeframe().getCode(), event.latestCandleTime(), event.requestId());
        processor.process(event.timeframe(), event.candles());
    }
}
