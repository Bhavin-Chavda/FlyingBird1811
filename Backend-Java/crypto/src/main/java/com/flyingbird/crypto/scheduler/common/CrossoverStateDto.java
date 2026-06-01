package com.flyingbird.crypto.scheduler.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Crossover State DTO
 *
 * Immutable snapshot of a job's latest EMA-stack signal evaluation, returned by
 * the market-data read APIs. Built from a job store under its read lock.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrossoverStateDto {

    private String jobId;
    private long calls;
    private String lastSignal;        // BULLISH / BEARISH / NEUTRAL / null
    private String lastCandleTime;    // candle time (IST string)
    private String lastCheckedAtIst;
}
