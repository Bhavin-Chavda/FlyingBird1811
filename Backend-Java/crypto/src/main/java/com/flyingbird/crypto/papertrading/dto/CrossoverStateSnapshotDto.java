package com.flyingbird.crypto.papertrading.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Immutable snapshot of the latest EMA-stack crossover signal of all four scheduler
 * timeframes at a single instant. Captured once when a paper trade is created and copied
 * onto the {@code PaperTrade} row; never updated afterwards.
 *
 * <p>Each value is one of {@code BULLISH} / {@code BEARISH} / {@code NEUTRAL}
 * (the project's {@code SchedulerConstants.SIGNAL_*} string representation — no enum is
 * introduced). A timeframe whose state is not yet available defaults to {@code NEUTRAL}.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrossoverStateSnapshotDto {

    private String oneMinute;
    private String fiveMinute;
    private String fifteenMinute;
    private String oneHour;
}
