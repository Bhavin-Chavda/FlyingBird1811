package com.flyingbird.crypto.scheduler.fifteenMinuteCandle;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

/**
 * 15-minute job's own Spring Batch step body. Records each run in the common
 * BATCH_* history tables.
 */
@Component
@RequiredArgsConstructor
public class FifteenMinuteCandleTasklet implements Tasklet {

    private final FifteenMinuteCandleService fifteenMinuteCandleService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        fifteenMinuteCandleService.run();
        return RepeatStatus.FINISHED;
    }
}
