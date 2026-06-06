package com.flyingbird.crypto.papertrading.service.impl;

import com.flyingbird.crypto.papertrading.dto.CrossoverStateSnapshotDto;
import com.flyingbird.crypto.papertrading.service.CrossoverStateSnapshotService;
import com.flyingbird.crypto.scheduler.common.CrossoverStateDto;
import com.flyingbird.crypto.scheduler.common.SchedulerConstants;
import com.flyingbird.crypto.scheduler.fifteenMinuteCandle.FifteenMinuteCandleStore;
import com.flyingbird.crypto.scheduler.fiveMinuteCandle.FiveMinuteCandleStore;
import com.flyingbird.crypto.scheduler.hourlyCandle.HourlyCandleStore;
import com.flyingbird.crypto.scheduler.oneMinuteCandle.OneMinuteCandleStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Default {@link CrossoverStateSnapshotService}. Depends on the four scheduler <b>stores</b>
 * (leaf beans), not their services — this keeps the dependency one-directional
 * (paper-trading → scheduler stores) and avoids any circular bean wiring with the
 * detection/creation pipeline.
 *
 * <p>{@code store.crossoverSnapshot()} returns an immutable {@link CrossoverStateDto} taken
 * under the store's read lock, so this is cheap and thread-safe from the async detection
 * thread. A {@code null}/unknown signal normalizes to {@code NEUTRAL} (matching the rest of
 * the scheduler, which defaults to NEUTRAL before the first evaluation).</p>
 */
@Service
@RequiredArgsConstructor
public class CrossoverStateSnapshotServiceImpl implements CrossoverStateSnapshotService {

    private final OneMinuteCandleStore oneMinuteCandleStore;
    private final FiveMinuteCandleStore fiveMinuteCandleStore;
    private final FifteenMinuteCandleStore fifteenMinuteCandleStore;
    private final HourlyCandleStore hourlyCandleStore;

    @Override
    public CrossoverStateSnapshotDto capture() {
        return CrossoverStateSnapshotDto.builder()
                .oneMinute(normalize(oneMinuteCandleStore.crossoverSnapshot()))
                .fiveMinute(normalize(fiveMinuteCandleStore.crossoverSnapshot()))
                .fifteenMinute(normalize(fifteenMinuteCandleStore.crossoverSnapshot()))
                .oneHour(normalize(hourlyCandleStore.crossoverSnapshot()))
                .build();
    }

    /** Map a store snapshot to a canonical BULLISH/BEARISH/NEUTRAL string (default NEUTRAL). */
    private String normalize(CrossoverStateDto state) {
        String signal = state == null ? null : state.getLastSignal();
        if (SchedulerConstants.SIGNAL_BULLISH.equals(signal)) {
            return SchedulerConstants.SIGNAL_BULLISH;
        }
        if (SchedulerConstants.SIGNAL_BEARISH.equals(signal)) {
            return SchedulerConstants.SIGNAL_BEARISH;
        }
        return SchedulerConstants.SIGNAL_NEUTRAL;
    }
}
