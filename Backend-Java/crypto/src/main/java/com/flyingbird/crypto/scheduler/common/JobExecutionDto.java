package com.flyingbird.crypto.scheduler.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Job Execution DTO
 *
 * One durable scheduler run from the common Spring Batch history tables
 * (BATCH_JOB_EXECUTION), returned by {@code GET /api/scheduler/history}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobExecutionDto {

    private Long executionId;
    private String jobName;       // batch job name (oneMinuteCandleJob / ...)
    private String status;        // COMPLETED / FAILED / STARTED ...
    private String exitCode;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private Long durationMs;
}
