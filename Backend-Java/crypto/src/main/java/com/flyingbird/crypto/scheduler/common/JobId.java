package com.flyingbird.crypto.scheduler.common;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Scheduler job-id path-variable enum for the scheduler status APIs — the URL
 * accepts ONLY the canonical id fb_1m_job / fb_5m_job / fb_15m_job (a bare
 * timeframe like {@code 1m} is rejected as 400).
 *
 * Mapped 1:1 to {@link Timeframe} via {@link #toTimeframe()} so scheduler data
 * and market data can be correlated without any separate mapping table.
 */
public enum JobId {

    FB_1M("fb_1m_job"),
    FB_5M("fb_5m_job"),
    FB_15M("fb_15m_job");

    private final String code;

    JobId(String code) {
        this.code = code;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    /** The market-data {@link Timeframe} this job produces. */
    public Timeframe toTimeframe() {
        return switch (this) {
            case FB_1M -> Timeframe.ONE_MINUTE;
            case FB_5M -> Timeframe.FIVE_MINUTE;
            case FB_15M -> Timeframe.FIFTEEN_MINUTE;
        };
    }

    public static JobId fromCode(String code) {
        for (JobId j : values()) {
            if (j.code.equalsIgnoreCase(code)) {
                return j;
            }
        }
        throw new IllegalArgumentException(
                "Unknown jobId '" + code + "' (expected fb_1m_job, fb_5m_job or fb_15m_job)");
    }
}
