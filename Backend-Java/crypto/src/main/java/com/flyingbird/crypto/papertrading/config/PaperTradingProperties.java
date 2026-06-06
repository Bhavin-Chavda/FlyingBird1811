package com.flyingbird.crypto.papertrading.config;

import com.flyingbird.crypto.scheduler.common.Timeframe;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Binds {@code paper-trading.*} config. Pattern detection is OFF for every
 * timeframe by default — it only runs where explicitly enabled. Trade evaluation
 * of OPEN trades is independent of these flags (it always runs on each 1m candle).
 */
@Data
@Component
@ConfigurationProperties(prefix = "paper-trading")
public class PaperTradingProperties {

    private PatternDetector patternDetector = new PatternDetector();
    private Evaluator evaluator = new Evaluator();

    @Data
    public static class Evaluator {
        /** Discard OPEN trades that missed evaluation for too long (server downtime/failure). */
        private boolean discardStaleOpenTradesEnabled = true;
        /** Minutes since last evaluation after which an OPEN trade is treated as stale → DISCARDED. */
        private long staleOpenTradeMinutes = 60;
    }

    @Data
    public static class PatternDetector {
        /** Per-timeframe enable flags (all default false). */
        private Enabled enabled = new Enabled();
        /** Run detection on a bounded async executor (vs synchronously on the scheduler thread). */
        private boolean asyncEnabled = true;
        /** Minimum confidence score for a detected pattern to become a trade. */
        private double minConfidence = 0.60;
        /** Skip a new trade if an identical one exists within this many recent candles. */
        private int duplicateWindowCandles = 10;
        /** Require volume confirmation on breakout candles (where volume is available). */
        private boolean useVolumeConfirmation = false;
        /** ATR lookback period. */
        private int atrPeriod = 14;
        /** Pivot detection left/right window. */
        private int pivotLeft = 3;
        private int pivotRight = 3;
        /** Conservative stop placement (e.g. head/extreme instead of right shoulder). */
        private boolean conservativeStops = false;

        @Data
        public static class Enabled {
            private boolean oneMinute = false;
            private boolean fiveMinute = false;
            private boolean fifteenMinute = false;
            private boolean oneHour = false;
        }
    }

    /** Whether pattern detection is enabled for the given timeframe. */
    public boolean isDetectionEnabled(Timeframe timeframe) {
        PatternDetector.Enabled e = patternDetector.getEnabled();
        return switch (timeframe) {
            case ONE_MINUTE -> e.isOneMinute();
            case FIVE_MINUTE -> e.isFiveMinute();
            case FIFTEEN_MINUTE -> e.isFifteenMinute();
            case ONE_HOUR -> e.isOneHour();
        };
    }
}
