package com.flyingbird.crypto.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Notification (email) Properties
 *
 * Binds `notification.email.*` — crossover signal email recipients/config,
 * migrated from the Python mailor.py env vars. Disabled by default so the app
 * runs without SMTP configured.
 *
 * {@link #enabled} is the global master switch (checked in MailServiceImpl).
 * {@link #signal} adds per-timeframe signal-email toggles so each scheduler job
 * (1m/5m/15m/1h) can have its signal emails enabled/disabled independently —
 * all default {@code true} so existing behavior is preserved.
 */
@Data
@Component
@ConfigurationProperties(prefix = "notification.email")
public class NotificationProperties {

    private boolean enabled = false;
    private String from;
    private List<String> to = new ArrayList<>();
    private List<String> cc = new ArrayList<>();
    private List<String> bcc = new ArrayList<>();
    private String subjectPrefix = "";

    /** Per-timeframe signal-email enable flags (all default true). */
    private Signal signal = new Signal();

    @Data
    public static class Signal {
        private boolean oneMinute = true;
        private boolean fiveMinute = true;
        private boolean fifteenMinute = true;
        private boolean oneHour = true;
    }

    /**
     * Whether signal email is enabled for a given resolution code
     * ("1m" / "5m" / "15m" / "1h"). Unknown codes return {@code true}
     * (fail-open) so behavior is never silently blocked.
     */
    public boolean isEmailEnabledFor(String resolution) {
        if (resolution == null) {
            return true;
        }
        return switch (resolution) {
            case "1m" -> signal.isOneMinute();
            case "5m" -> signal.isFiveMinute();
            case "15m" -> signal.isFifteenMinute();
            case "1h" -> signal.isOneHour();
            default -> true;
        };
    }
}
