package com.flyingbird.crypto.scheduler.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Job Status DTO
 *
 * Immutable snapshot of a scheduler job's live status, returned by the scheduler
 * status APIs. Built under the read lock so it is always internally consistent.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobStatusDto {

    private String jobId;
    private String jobName;
    private String cron;
    private String threadName;

    private boolean running;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastEndTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastSuccessTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastFailureTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime nextRunTime;

    private long totalRuns;
    private long totalFailures;
    private Long lastDurationMs;
    private Long lastDataCount;
    private String lastErrorMessage;
}
