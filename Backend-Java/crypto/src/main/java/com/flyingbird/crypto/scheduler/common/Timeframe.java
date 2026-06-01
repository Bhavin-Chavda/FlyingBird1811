package com.flyingbird.crypto.scheduler.common;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Timeframe path-variable enum for the market-data APIs — the URL accepts ONLY
 * 1m / 5m / 15m (an {@code fb_*} job id is rejected as 400).
 *
 * Mapped 1:1 to {@link JobId} via {@link #toJobId()} so market data and scheduler
 * data can be correlated without any separate mapping table.
 */
public enum Timeframe {

    ONE_MINUTE("1m"),
    FIVE_MINUTE("5m"),
    FIFTEEN_MINUTE("15m");

    private final String code;

    Timeframe(String code) {
        this.code = code;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    /** The scheduler {@link JobId} that owns this timeframe. */
    public JobId toJobId() {
        return switch (this) {
            case ONE_MINUTE -> JobId.FB_1M;
            case FIVE_MINUTE -> JobId.FB_5M;
            case FIFTEEN_MINUTE -> JobId.FB_15M;
        };
    }

    public static Timeframe fromCode(String code) {
        for (Timeframe t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown timeframe '" + code + "' (expected 1m, 5m or 15m)");
    }
}
