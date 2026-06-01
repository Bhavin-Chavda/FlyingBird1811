package com.flyingbird.crypto.scheduler.fiveMinuteCandle;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 5-minute job's OWN Spring Batch job/step (no shared batch job).
 * Executions are persisted to the common BATCH_* tables under job name
 * "fiveMinuteCandleJob".
 */
@Configuration
public class FiveMinuteCandleBatchConfig {

    public static final String JOB_NAME = "fiveMinuteCandleJob";

    @Bean
    public Step fiveMinuteCandleStep(JobRepository jobRepository,
                                     PlatformTransactionManager transactionManager,
                                     FiveMinuteCandleTasklet fiveMinuteCandleTasklet) {
        return new StepBuilder("fiveMinuteCandleStep", jobRepository)
                .tasklet(fiveMinuteCandleTasklet, transactionManager)
                .build();
    }

    @Bean
    public Job fiveMinuteCandleJob(JobRepository jobRepository, Step fiveMinuteCandleStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(fiveMinuteCandleStep)
                .build();
    }
}
