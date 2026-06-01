package com.flyingbird.crypto.scheduler.common;

/**
 * Job Details Service
 *
 * Aggregates one job's live status, latest crossover state and last few candles
 * into a single {@link JobDetailsResponseDto}. Reads safe, immutable snapshots
 * from the shared {@link JobStatusService} and the per-job stores — no locks are
 * held while building the response.
 */
public interface JobDetailsService {

    /**
     * Build the aggregate details for one job.
     *
     * @param jobId the scheduler job id (already validated/mapped by the controller)
     * @return the populated {@link JobDetailsResponseDto}
     */
    JobDetailsResponseDto getJobDetails(JobId jobId);
}
