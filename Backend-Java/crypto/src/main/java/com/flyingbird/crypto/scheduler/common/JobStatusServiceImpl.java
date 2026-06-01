package com.flyingbird.crypto.scheduler.common;

import com.flyingbird.crypto.exception.JobNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Job Status Service Implementation
 *
 * In-memory LIVE status (running / next-run / last duration / counters), guarded
 * by a {@link ReentrantReadWriteLock}:
 * - status reads (APIs, on the request/main thread) take the READ lock,
 * - job callbacks (register/start/success/failure) take the WRITE lock.
 * DTO snapshots are built under the read lock so APIs never see a half-written
 * status. Durable run HISTORY lives in the Spring Batch BATCH_* tables (queried
 * by the controller via JobRepository), not here.
 */
@Slf4j
@Service
public class JobStatusServiceImpl implements JobStatusService {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private final Map<String, JobState> states = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public void register(String jobId, String jobName, String cron, String threadName) {
        lock.writeLock().lock();
        try {
            JobState s = states.computeIfAbsent(jobId, k -> new JobState());
            s.jobName = jobName;
            s.cron = cron;
            s.threadName = threadName;
            s.running = false;
            log.info("Registered scheduler job | jobId={} | cron={} | thread={}", jobId, cron, threadName);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean tryStart(String jobId, String threadName) {
        lock.writeLock().lock();
        try {
            JobState s = states.get(jobId);
            if (s == null) {
                log.warn("tryStart for unknown jobId={}", jobId);
                return false;
            }
            if (s.running) {
                log.warn("Job already running, skipping overlapping run | jobId={}", jobId);
                return false;
            }
            s.running = true;
            s.threadName = threadName;
            s.lastStart = LocalDateTime.now(IST);
            s.totalRuns++;
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void markSuccess(String jobId, long dataCount) {
        lock.writeLock().lock();
        try {
            JobState s = states.get(jobId);
            if (s == null) {
                return;
            }
            LocalDateTime now = LocalDateTime.now(IST);
            s.running = false;
            s.lastEnd = now;
            s.lastSuccess = now;
            s.lastDataCount = dataCount;
            s.lastDurationMs = durationMs(s.lastStart, now);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void markFailure(String jobId, String errorMessage) {
        lock.writeLock().lock();
        try {
            JobState s = states.get(jobId);
            if (s == null) {
                return;
            }
            LocalDateTime now = LocalDateTime.now(IST);
            s.running = false;
            s.lastEnd = now;
            s.lastFailure = now;
            s.lastError = errorMessage;
            s.totalFailures++;
            s.lastDurationMs = durationMs(s.lastStart, now);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public JobStatusDto getStatus(String jobId) {
        lock.readLock().lock();
        try {
            JobState s = states.get(jobId);
            if (s == null) {
                throw new JobNotFoundException("Scheduler job '" + jobId + "' not found");
            }
            return toDto(jobId, s);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<JobStatusDto> getAllStatuses() {
        lock.readLock().lock();
        try {
            List<JobStatusDto> out = new ArrayList<>();
            states.forEach((id, s) -> out.add(toDto(id, s)));
            return out;
        } finally {
            lock.readLock().unlock();
        }
    }

    private JobStatusDto toDto(String jobId, JobState s) {
        return JobStatusDto.builder()
                .jobId(jobId)
                .jobName(s.jobName)
                .cron(s.cron)
                .threadName(s.threadName)
                .running(s.running)
                .lastStartTime(s.lastStart)
                .lastEndTime(s.lastEnd)
                .lastSuccessTime(s.lastSuccess)
                .lastFailureTime(s.lastFailure)
                .nextRunTime(computeNextRun(s.cron))
                .totalRuns(s.totalRuns)
                .totalFailures(s.totalFailures)
                .lastDurationMs(s.lastDurationMs)
                .lastDataCount(s.lastDataCount)
                .lastErrorMessage(s.lastError)
                .build();
    }

    private Long durationMs(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return null;
        }
        return Duration.between(start, end).toMillis();
    }

    private LocalDateTime computeNextRun(String cron) {
        try {
            return CronExpression.parse(cron).next(LocalDateTime.now(IST));
        } catch (Exception e) {
            return null;
        }
    }

    /** Internal mutable state — only touched while holding the lock. */
    private static final class JobState {
        private String jobName;
        private String cron;
        private String threadName;
        private boolean running;
        private LocalDateTime lastStart;
        private LocalDateTime lastEnd;
        private LocalDateTime lastSuccess;
        private LocalDateTime lastFailure;
        private long totalRuns;
        private long totalFailures;
        private Long lastDurationMs;
        private Long lastDataCount;
        private String lastError;
    }
}
