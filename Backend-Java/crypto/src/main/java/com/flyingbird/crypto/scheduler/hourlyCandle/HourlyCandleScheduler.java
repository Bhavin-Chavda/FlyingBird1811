package com.flyingbird.crypto.scheduler.hourlyCandle;

import com.flyingbird.crypto.config.SchedulerProperties;
import com.flyingbird.crypto.scheduler.common.JobStatusService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

/**
 * Hourly candle scheduler (owned by the 1h job only). Own dedicated thread
 * {@code scheduler-1h-candle}; launches its own {@code hourlyCandleJob}
 * (resolved by parameter name). No base class, no shared runner.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HourlyCandleScheduler {

    private static final String JOB_ID = "fb_1h_job";
    private static final String JOB_NAME = "Flying Bird 1h";
    private static final String THREAD_NAME = "scheduler-1h-candle";

    private final HourlyCandleService hourlyCandleService;
    private final JobStatusService jobStatusService;
    private final SchedulerProperties props;
    private final JobOperator jobOperator;
    private final Job hourlyCandleJob;

    private ThreadPoolTaskScheduler executor;

    @PostConstruct
    public void start() {
        String cron = props.getCron().getOneHour();
        jobStatusService.register(JOB_ID, JOB_NAME, cron, THREAD_NAME);

        executor = new ThreadPoolTaskScheduler();
        executor.setPoolSize(1);
        executor.setThreadNamePrefix(THREAD_NAME + "-");
        executor.setWaitForTasksToCompleteOnShutdown(false);
        executor.setAwaitTerminationSeconds(10);
        executor.setErrorHandler(t -> log.error("[{}] unhandled scheduler error: {}", THREAD_NAME, t.getMessage(), t));
        executor.initialize();

        if (props.isSeedOnStartup()) {
            executor.execute(this::initialSeed); // seed the 300-candle buffer at startup, on this job's own thread
        }
        executor.schedule(this::run, new CronTrigger(cron, ZoneId.of(props.getTimezone())));
        log.info("[{}] scheduled | cron={} | timezone={}", THREAD_NAME, cron, props.getTimezone());
    }

    private void initialSeed() {
        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                hourlyCandleService.seed();
                log.info("[{}] initial buffer seeded ({} candles)", THREAD_NAME, hourlyCandleService.bufferSize());
                return;
            } catch (Exception e) {
                log.warn("[{}] initial seed attempt {}/{} failed: {}", THREAD_NAME, attempt, maxAttempts, e.getMessage());
                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(3000L);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }
        log.error("[{}] initial seeding failed after {} attempts; buffer will refill on the next scheduled tick",
                THREAD_NAME, maxAttempts);
    }

    private void run() {
        String thread = Thread.currentThread().getName();
        if (!jobStatusService.tryStart(JOB_ID, thread)) {
            return;
        }
        long startedAt = System.currentTimeMillis();
        log.info("[{}] Started hourly candle fetch", thread);
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("runAt", System.currentTimeMillis())
                    .toJobParameters();
            JobExecution execution = jobOperator.start(hourlyCandleJob, params);

            if (execution.getStatus() == BatchStatus.COMPLETED) {
                int records = hourlyCandleService.bufferSize();
                jobStatusService.markSuccess(JOB_ID, records);
                log.info("[{}] Completed hourly candle fetch in {}ms, records={}",
                        thread, System.currentTimeMillis() - startedAt, records);
            } else {
                jobStatusService.markFailure(JOB_ID, "Batch status " + execution.getStatus()
                        + " " + execution.getAllFailureExceptions());
                log.error("[{}] Failed hourly candle fetch | batch status={}", thread, execution.getStatus());
            }
        } catch (Exception e) {
            jobStatusService.markFailure(JOB_ID, e.getMessage());
            log.error("[{}] Failed hourly candle fetch: {}", thread, e.getMessage(), e);
        }
    }

    @PreDestroy
    public void stop() {
        if (executor != null) {
            executor.shutdown();
        }
    }
}
