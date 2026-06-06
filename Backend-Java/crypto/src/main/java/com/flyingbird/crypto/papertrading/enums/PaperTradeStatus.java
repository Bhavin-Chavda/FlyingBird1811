package com.flyingbird.crypto.papertrading.enums;

/**
 * Lifecycle status of a paper trade. OPEN is the only non-terminal status;
 * CLOSED (normal TP4/SL outcome) and DISCARDED (recovery/staleness/invalid —
 * not a normal outcome) are terminal and excluded from evaluation.
 */
public enum PaperTradeStatus {
    OPEN,
    CLOSED,
    DISCARDED
}
