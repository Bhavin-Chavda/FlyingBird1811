package com.flyingbird.crypto.papertrading.service;

import com.flyingbird.crypto.papertrading.dto.CrossoverStateSnapshotDto;

/**
 * Reads the latest crossover signal of the 1m / 5m / 15m / 1h scheduler stores and
 * returns them as a single immutable snapshot for stamping onto a new paper trade.
 *
 * <p>Implementations must be safe to call from the async pattern-detection thread and must
 * not perform any slow/external work — values are read from the in-memory job stores under
 * their read locks only.</p>
 */
public interface CrossoverStateSnapshotService {

    /** Capture the current 1m/5m/15m/1h crossover states. Never returns {@code null} fields. */
    CrossoverStateSnapshotDto capture();
}
