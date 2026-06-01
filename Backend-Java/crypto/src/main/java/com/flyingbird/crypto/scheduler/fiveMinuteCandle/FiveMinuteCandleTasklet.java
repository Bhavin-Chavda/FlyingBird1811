package com.flyingbird.crypto.scheduler.fiveMinuteCandle;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

/**
 * 5-minute job's own Spring Batch step body. Runs one update cycle so the
 * execution is recorded in the common BATCH_* history tables.
 */
@Component
@RequiredArgsConstructor
public class FiveMinuteCandleTasklet implements Tasklet {

    private final FiveMinuteCandleService fiveMinuteCandleService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        fiveMinuteCandleService.run();
        return RepeatStatus.FINISHED;
    }
}
