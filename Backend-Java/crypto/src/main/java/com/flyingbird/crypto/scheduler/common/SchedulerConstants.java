package com.flyingbird.crypto.scheduler.common;

/**
 * Scheduler Constants
 *
 * Shared, immutable constants for the scheduler module. No mutable state.
 */
public final class SchedulerConstants {

    private SchedulerConstants() {
    }

    public static final String SIGNAL_BULLISH = "BULLISH";
    public static final String SIGNAL_BEARISH = "BEARISH";
    public static final String SIGNAL_NEUTRAL = "NEUTRAL";

    public static final String EXECUTION_SUCCESS = "SUCCESS";
    public static final String EXECUTION_FAILURE = "FAILURE";

    /** Max in-memory execution-history records kept per job. */
    public static final int MAX_HISTORY_PER_JOB = 50;
}
