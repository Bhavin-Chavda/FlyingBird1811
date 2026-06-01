package com.flyingbird.crypto.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Scheduler Properties
 *
 * Binds `scheduler.*` from application.yaml — the timezone, the per-job cron
 * expressions (single source of truth) and the job-details API settings. Each
 * job scheduler reads its own cron via the relevant getter (no shared dispatch).
 */
@Data
@Component
@ConfigurationProperties(prefix = "scheduler")
public class SchedulerProperties {

    private String timezone = "Asia/Kolkata";
    /** Seed each job's buffer at startup on its own thread (disable in tests). */
    private boolean seedOnStartup = true;
    private Cron cron = new Cron();
    private JobDetails jobDetails = new JobDetails();

    @Data
    public static class Cron {
        private String oneMin;
        private String fiveMin;
        private String fifteenMin;
    }

    @Data
    public static class JobDetails {
        /** How many most-recent candles the job-details API returns per job. */
        private int candleCount = 5;
    }
}
