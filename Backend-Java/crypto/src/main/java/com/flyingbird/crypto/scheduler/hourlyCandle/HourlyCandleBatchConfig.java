package com.flyingbird.crypto.scheduler.hourlyCandle;

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
 * Hourly job's OWN Spring Batch job/step. Executions persist to the common
 * BATCH_* tables under job name "hourlyCandleJob".
 */
@Configuration
public class HourlyCandleBatchConfig {

    public static final String JOB_NAME = "hourlyCandleJob";

    @Bean
    public Step hourlyCandleStep(JobRepository jobRepository,
                                 PlatformTransactionManager transactionManager,
                                 HourlyCandleTasklet hourlyCandleTasklet) {
        return new StepBuilder("hourlyCandleStep", jobRepository)
                .tasklet(hourlyCandleTasklet, transactionManager)
                .build();
    }

    @Bean
    public Job hourlyCandleJob(JobRepository jobRepository, Step hourlyCandleStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(hourlyCandleStep)
                .build();
    }
}
