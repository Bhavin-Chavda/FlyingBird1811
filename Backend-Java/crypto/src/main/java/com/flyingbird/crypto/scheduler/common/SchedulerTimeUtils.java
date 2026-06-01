package com.flyingbird.crypto.scheduler.common;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Scheduler Time Utilities
 *
 * Stateless IST time helpers shared by the jobs.
 */
public final class SchedulerTimeUtils {

    private SchedulerTimeUtils() {
    }

    public static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private static final DateTimeFormatter LABEL_FMT =
            DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a", Locale.ENGLISH);

    /** Human-readable IST timestamp label, e.g. "06/01/2026 05:30:01 PM IST". */
    public static String nowIstLabel() {
        return LocalDateTime.now(IST).format(LABEL_FMT) + " IST";
    }
}
