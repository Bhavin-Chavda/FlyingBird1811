package com.flyingbird.crypto.scheduler.oneMinuteCandle;

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
 * 1-minute job's OWN Spring Batch job/step. Executions persist to the common
 * BATCH_* tables under job name "oneMinuteCandleJob".
 */
@Configuration
public class OneMinuteCandleBatchConfig {

    public static final String JOB_NAME = "oneMinuteCandleJob";

    @Bean
    public Step oneMinuteCandleStep(JobRepository jobRepository,
                                    PlatformTransactionManager transactionManager,
                                    OneMinuteCandleTasklet oneMinuteCandleTasklet) {
        return new StepBuilder("oneMinuteCandleStep", jobRepository)
                .tasklet(oneMinuteCandleTasklet, transactionManager)
                .build();
    }

    @Bean
    public Job oneMinuteCandleJob(JobRepository jobRepository, Step oneMinuteCandleStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(oneMinuteCandleStep)
                .build();
    }
}
