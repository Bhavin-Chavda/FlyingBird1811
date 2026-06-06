package com.flyingbird.crypto.papertrading.enums;

/**
 * Classical chart patterns the detector can recognise.
 *
 * <p>{@link #direction} is the trade direction the pattern implies. {@link #implemented}
 * marks Phase-1 patterns that have real detection logic; the rest are reserved enum
 * values for Phase 2 (so the DB/DTO contract is stable) but are not detected yet.</p>
 */
public enum ChartPatternName {

    // ---- Bearish (Phase 1 implemented) ----
    DOUBLE_TOP(TradeDirection.BEARISH, true),
    HEAD_AND_SHOULDERS(TradeDirection.BEARISH, true),
    DESCENDING_TRIANGLE(TradeDirection.BEARISH, true),
    RISING_WEDGE_BREAKDOWN(TradeDirection.BEARISH, true),
    BEAR_FLAG(TradeDirection.BEARISH, true),
    RECTANGLE_BREAKDOWN_DOWN(TradeDirection.BEARISH, true),

    // ---- Bullish (Phase 1 implemented) ----
    DOUBLE_BOTTOM(TradeDirection.BULLISH, true),
    INVERSE_HEAD_AND_SHOULDERS(TradeDirection.BULLISH, true),
    ASCENDING_TRIANGLE(TradeDirection.BULLISH, true),
    FALLING_WEDGE_BREAKOUT(TradeDirection.BULLISH, true),
    BULL_FLAG(TradeDirection.BULLISH, true),
    RECTANGLE_BREAKOUT_UP(TradeDirection.BULLISH, true),

    // ---- Phase 2 (reserved; not yet detected) ----
    TRIPLE_TOP(TradeDirection.BEARISH, false),
    TRIPLE_BOTTOM(TradeDirection.BULLISH, false),
    BULL_PENNANT(TradeDirection.BULLISH, false),
    BEAR_PENNANT(TradeDirection.BEARISH, false),
    CUP_AND_HANDLE(TradeDirection.BULLISH, false),
    INVERTED_CUP_AND_HANDLE(TradeDirection.BEARISH, false),
    ROUNDING_BOTTOM(TradeDirection.BULLISH, false),
    ROUNDING_TOP(TradeDirection.BEARISH, false);

    private final TradeDirection direction;
    private final boolean implemented;

    ChartPatternName(TradeDirection direction, boolean implemented) {
        this.direction = direction;
        this.implemented = implemented;
    }

    public TradeDirection getDirection() {
        return direction;
    }

    public boolean isImplemented() {
        return implemented;
    }
}
