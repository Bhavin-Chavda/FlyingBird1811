package com.flyingbird.crypto.scheduler.common;

import com.flyingbird.crypto.config.SchedulerProperties;
import com.flyingbird.crypto.marketdata.model.Candle;
import com.flyingbird.crypto.scheduler.fifteenMinuteCandle.FifteenMinuteCandleService;
import com.flyingbird.crypto.scheduler.fiveMinuteCandle.FiveMinuteCandleService;
import com.flyingbird.crypto.scheduler.hourlyCandle.HourlyCandleService;
import com.flyingbird.crypto.scheduler.oneMinuteCandle.OneMinuteCandleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Job Details Service implementation.
 *
 * Dispatches to the per-job service that owns each store (no shared runner, no
 * base class). Each call reads:
 * <ul>
 *   <li>live status from the RW-locked {@link JobStatusService}</li>
 *   <li>crossover state + buffer from the relevant per-job store (read-locked)</li>
 * </ul>
 * and copies the last 5 candles into a fresh list so no mutable internal
 * collection is exposed.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobDetailsServiceImpl implements JobDetailsService {

    private final SchedulerProperties schedulerProperties;
    private final JobStatusService jobStatusService;
    private final OneMinuteCandleService oneMinuteCandleService;
    private final FiveMinuteCandleService fiveMinuteCandleService;
    private final FifteenMinuteCandleService fifteenMinuteCandleService;
    private final HourlyCandleService hourlyCandleService;

    @Override
    public JobDetailsResponseDto getJobDetails(JobId jobId) {
        log.info("Building job details | jobId={}", jobId.getCode());

        // Live status snapshot (RW-locked, internally consistent).
        JobStatusDto status = jobStatusService.getStatus(jobId.getCode());

        // Crossover state + buffer come from the per-job store this job owns.
        CrossoverStateDto crossover = switch (jobId) {
            case FB_1M -> oneMinuteCandleService.getCrossoverState();
            case FB_5M -> fiveMinuteCandleService.getCrossoverState();
            case FB_15M -> fifteenMinuteCandleService.getCrossoverState();
            case FB_1H -> hourlyCandleService.getCrossoverState();
        };

        List<Candle> buffer = switch (jobId) {
            case FB_1M -> oneMinuteCandleService.getBufferSnapshot();
            case FB_5M -> fiveMinuteCandleService.getBufferSnapshot();
            case FB_15M -> fifteenMinuteCandleService.getBufferSnapshot();
            case FB_1H -> hourlyCandleService.getBufferSnapshot();
        };

        List<Candle> candles = lastCandles(buffer);

        return JobDetailsResponseDto.builder()
                .jobId(jobId.getCode())
                .jobName(status != null ? status.getJobName() : jobId.getCode())
                .timeframe(jobId.toTimeframe().getCode())
                .status(status)
                .lastCrossOverState(crossover)
                .candles(candles)
                .build();
    }

    /**
     * Immutable copy of the last {@code scheduler.job-details.candle-count} candles
     * (oldest → newest).
     */
    private List<Candle> lastCandles(List<Candle> buffer) {
        if (buffer == null || buffer.isEmpty()) {
            return Collections.emptyList();
        }
        int count = Math.max(0, schedulerProperties.getJobDetails().getCandleCount());
        if (count == 0) {
            return Collections.emptyList();
        }
        int size = buffer.size();
        int from = Math.max(0, size - count);
        return new ArrayList<>(buffer.subList(from, size));
    }
}
