package com.flyingbird.crypto.scheduler.common;

import java.util.List;

/**
 * Job Status Service
 *
 * Shared, thread-safe live-status + recent-execution-history store for all
 * scheduler jobs. Each job owns its own scheduler/service/store; this is the one
 * shared piece for status (the user's design explicitly keeps it common).
 *
 * Writes come from the per-job scheduler threads; reads come from API request
 * threads. Implementations guard state so APIs always see consistent snapshots.
 */
public interface JobStatusService {

    /** Register a job once at startup so its status can be tracked. */
    void register(String jobId, String jobName, String cron, String threadName);

    /**
     * Atomically mark a job as running on the given thread.
     *
     * @return true if started; false if it was already running (overlap guard)
     *         or the job id is unknown.
     */
    boolean tryStart(String jobId, String threadName);

    /** Mark the current run successful, recording the data count held. */
    void markSuccess(String jobId, long dataCount);

    /** Mark the current run failed, recording the error message. */
    void markFailure(String jobId, String errorMessage);

    /** Consistent snapshot of one job's live status. */
    JobStatusDto getStatus(String jobId);

    /** Consistent snapshots of all registered jobs' live status. */
    List<JobStatusDto> getAllStatuses();
}
