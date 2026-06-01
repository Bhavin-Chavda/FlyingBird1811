package com.flyingbird.crypto.scheduler.fifteenMinuteCandle;

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
 * 15-minute job's OWN Spring Batch job/step. Executions persist to the common
 * BATCH_* tables under job name "fifteenMinuteCandleJob".
 */
@Configuration
public class FifteenMinuteCandleBatchConfig {

    public static final String JOB_NAME = "fifteenMinuteCandleJob";

    @Bean
    public Step fifteenMinuteCandleStep(JobRepository jobRepository,
                                        PlatformTransactionManager transactionManager,
                                        FifteenMinuteCandleTasklet fifteenMinuteCandleTasklet) {
        return new StepBuilder("fifteenMinuteCandleStep", jobRepository)
                .tasklet(fifteenMinuteCandleTasklet, transactionManager)
                .build();
    }

    @Bean
    public Job fifteenMinuteCandleJob(JobRepository jobRepository, Step fifteenMinuteCandleStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(fifteenMinuteCandleStep)
                .build();
    }
}
