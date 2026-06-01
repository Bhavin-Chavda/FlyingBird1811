package com.flyingbird.crypto.controller;

import com.flyingbird.crypto.scheduler.common.JobDetailsResponseDto;
import com.flyingbird.crypto.scheduler.common.JobDetailsService;
import com.flyingbird.crypto.scheduler.common.JobId;
import com.flyingbird.crypto.scheduler.common.Timeframe;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Job Details Controller (thin)
 *
 * Single aggregate read API for the dashboard Job Details page. The URL accepts
 * ONLY the {@link Timeframe} values 1m / 5m / 15m (an {@code fb_*} job id is
 * rejected as 400). The timeframe is mapped 1:1 to the scheduler {@link JobId}
 * via {@link Timeframe#toJobId()} so the response correlates scheduler status,
 * crossover state and candles without a separate lookup. JWT-protected; reads
 * thread-safe snapshots only. Business logic lives in {@link JobDetailsService}.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/jobs")
public class JobDetailsController {

    private final JobDetailsService jobDetailsService;

    @GetMapping("/{timeframe}/details")
    @Operation(summary = "Get full job details",
            description = "Aggregate details for one scheduler job by timeframe (1m / 5m / 15m): "
                    + "live status, latest crossover state and the last 5 candles.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job details retrieved",
                    content = @Content(schema = @Schema(implementation = JobDetailsResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid timeframe (expected 1m, 5m or 15m)"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
            @ApiResponse(responseCode = "404", description = "Job with given timeframe not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<JobDetailsResponseDto> getJobDetails(@PathVariable Timeframe timeframe) {
        JobId jobId = timeframe.toJobId();
        log.info("Job details requested | timeframe={} jobId={}", timeframe.getCode(), jobId.getCode());
        return ResponseEntity.ok(jobDetailsService.getJobDetails(jobId));
    }
}
