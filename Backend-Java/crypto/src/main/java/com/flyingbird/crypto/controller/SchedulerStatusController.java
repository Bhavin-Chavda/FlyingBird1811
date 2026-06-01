package com.flyingbird.crypto.controller;

import com.flyingbird.crypto.scheduler.common.JobExecutionDto;
import com.flyingbird.crypto.scheduler.common.JobId;
import com.flyingbird.crypto.scheduler.common.JobStatusDto;
import com.flyingbird.crypto.scheduler.common.JobStatusService;
import com.flyingbird.crypto.scheduler.fifteenMinuteCandle.FifteenMinuteCandleBatchConfig;
import com.flyingbird.crypto.scheduler.fiveMinuteCandle.FiveMinuteCandleBatchConfig;
import com.flyingbird.crypto.scheduler.oneMinuteCandle.OneMinuteCandleBatchConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Scheduler Status Controller (thin)
 *
 * Live job status from the in-memory {@link JobStatusService}; durable execution
 * history from the common Spring Batch {@code BATCH_*} tables via
 * {@link JobRepository} (across the three per-job job names). Reads run on the
 * request/main thread and are thread-safe (RW-locked status + DB reads).
 * JWT-protected.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/scheduler")
public class SchedulerStatusController {

    private static final String[] BATCH_JOB_NAMES = {
            OneMinuteCandleBatchConfig.JOB_NAME,
            FiveMinuteCandleBatchConfig.JOB_NAME,
            FifteenMinuteCandleBatchConfig.JOB_NAME
    };

    private final JobStatusService jobStatusService;
    private final JobRepository jobRepository;

    @GetMapping("/jobs")
    @Operation(summary = "List scheduler jobs", description = "Live runtime status of all candle scheduler jobs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job statuses retrieved",
                    content = @Content(schema = @Schema(implementation = JobStatusDto.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<List<JobStatusDto>> listJobs() {
        log.info("Scheduler jobs status requested");
        return ResponseEntity.ok(jobStatusService.getAllStatuses());
    }

    @GetMapping("/jobs/{jobId}")
    @Operation(summary = "Get scheduler job status",
            description = "Live runtime status of one scheduler job by jobId (fb_1m_job / fb_5m_job / fb_15m_job)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job status retrieved",
                    content = @Content(schema = @Schema(implementation = JobStatusDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid jobId"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
            @ApiResponse(responseCode = "404", description = "Job with given jobId not found")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<JobStatusDto> getJob(@PathVariable JobId jobId) {
        log.info("Scheduler job status requested | jobId={}", jobId.getCode());
        return ResponseEntity.ok(jobStatusService.getStatus(jobId.getCode()));
    }

    @GetMapping("/history")
    @Operation(summary = "Get scheduler job execution history",
            description = "Recent durable job executions from the common Spring Batch tables (newest first)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "History retrieved",
                    content = @Content(schema = @Schema(implementation = JobExecutionDto.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<List<JobExecutionDto>> history(@RequestParam(defaultValue = "20") int limit) {
        log.info("Scheduler job history requested | limit={}", limit);
        int cap = Math.max(1, limit);

        List<JobExecutionDto> out = new ArrayList<>();
        for (String jobName : BATCH_JOB_NAMES) {
            for (JobInstance instance : jobRepository.getJobInstances(jobName, 0, cap)) {
                for (JobExecution execution : jobRepository.getJobExecutions(instance)) {
                    out.add(toDto(jobName, execution));
                }
            }
        }
        out.sort(Comparator.comparing(JobExecutionDto::getExecutionId,
                Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        if (out.size() > cap) {
            out = out.subList(0, cap);
        }
        return ResponseEntity.ok(out);
    }

    private JobExecutionDto toDto(String jobName, JobExecution execution) {
        LocalDateTime start = execution.getStartTime();
        LocalDateTime end = execution.getEndTime();
        Long durationMs = (start != null && end != null) ? Duration.between(start, end).toMillis() : null;
        return JobExecutionDto.builder()
                .executionId(execution.getId())
                .jobName(jobName)
                .status(execution.getStatus().toString())
                .exitCode(execution.getExitStatus() != null ? execution.getExitStatus().getExitCode() : null)
                .startTime(start)
                .endTime(end)
                .durationMs(durationMs)
                .build();
    }
}
