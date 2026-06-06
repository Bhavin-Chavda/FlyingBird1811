package com.flyingbird.crypto.papertrading.event;

import com.flyingbird.crypto.marketdata.model.Candle;
import com.flyingbird.crypto.scheduler.common.Timeframe;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Published when a new closed candle is available and pattern detection is enabled
 * for that timeframe. Carries an immutable candle snapshot so the async listener
 * never touches the live scheduler store. One event per (timeframe, candle).
 */
public record PatternDetectionRequestedEvent(
        Timeframe timeframe,
        List<Candle> candles,
        String latestCandleTime,
        double latestClose,
        String requestId,
        LocalDateTime createdAt) {
}
