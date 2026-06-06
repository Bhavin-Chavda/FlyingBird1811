package com.flyingbird.crypto.papertrading.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Parses the existing in-memory {@code Candle.time} string ("yyyyMMdd_HHmmss",
 * IST, as produced by the Delta client) into a {@link LocalDateTime} for
 * persistence. Stateless.
 */
public final class CandleTimeUtils {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private CandleTimeUtils() {
    }

    /** Parse "yyyyMMdd_HHmmss" → LocalDateTime, or null if blank/unparseable. */
    public static LocalDateTime parse(String candleTime) {
        if (candleTime == null || candleTime.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(candleTime.trim(), FMT);
        } catch (Exception e) {
            return null;
        }
    }
}
