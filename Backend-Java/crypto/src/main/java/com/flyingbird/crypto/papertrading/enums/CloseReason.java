package com.flyingbird.crypto.papertrading.enums;

/**
 * Why a paper trade was closed or discarded.
 *
 * <p>Normal CLOSED outcomes: {@link #STOP_LOSS}, {@link #TP4}, {@link #MANUAL}.
 * DISCARDED (recovery) reasons: {@link #INVALID_PRICE_STATE},
 * {@link #STALE_OPEN_TRADE}, {@link #AMBIGUOUS_RECOVERY_STATE},
 * {@link #DISCARDED_RECOVERY_GAP}.</p>
 */
public enum CloseReason {
    // ---- normal CLOSED outcomes ----
    STOP_LOSS,
    TP4,
    MANUAL,
    INVALID,

    // ---- DISCARDED (recovery / staleness / invalid state) ----
    INVALID_PRICE_STATE,
    STALE_OPEN_TRADE,
    AMBIGUOUS_RECOVERY_STATE,
    DISCARDED_RECOVERY_GAP
}
