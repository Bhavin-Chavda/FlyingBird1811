package com.flyingbird.crypto.scheduler.common;

import com.flyingbird.crypto.marketdata.model.Candle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Job Details Response DTO
 *
 * Single, frontend-friendly aggregate for ONE scheduler job, returned by
 * {@code GET /api/jobs/{timeframe}/details}. It bundles everything the Job
 * Details card + popup need in one round-trip:
 *
 * <ul>
 *   <li>{@code status}            — the full live {@link JobStatusDto} snapshot</li>
 *   <li>{@code lastCrossOverState} — the job's latest EMA-stack signal evaluation</li>
 *   <li>{@code lastFiveCandles}   — an immutable copy of the 5 most recent candles</li>
 * </ul>
 *
 * All nested values are immutable snapshots built under the relevant read locks,
 * so the response is always internally consistent even while scheduler threads
 * are writing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobDetailsResponseDto {

    /** Canonical scheduler job id (fb_1m_job / fb_5m_job / fb_15m_job). */
    private String jobId;

    /** Human-readable job name (from the live status registration). */
    private String jobName;

    /** Market-data timeframe code (1m / 5m / 15m). */
    private String timeframe;

    /** Full live runtime status (same shape as the scheduler status APIs). */
    private JobStatusDto status;

    /** Latest crossover signal evaluation for this job (may be null before first run). */
    private CrossoverStateDto lastCrossOverState;

    /** Up to the 5 most recent candles (oldest → newest); empty before seeding. */
    private List<Candle> lastFiveCandles;
}
