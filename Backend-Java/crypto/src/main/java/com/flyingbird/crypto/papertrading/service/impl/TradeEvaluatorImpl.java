package com.flyingbird.crypto.papertrading.service.impl;

import com.flyingbird.crypto.marketdata.model.Candle;
import com.flyingbird.crypto.papertrading.config.PaperTradingProperties;
import com.flyingbird.crypto.papertrading.entity.PaperTrade;
import com.flyingbird.crypto.papertrading.enums.CloseReason;
import com.flyingbird.crypto.papertrading.enums.PaperTradeStatus;
import com.flyingbird.crypto.papertrading.enums.TradeDirection;
import com.flyingbird.crypto.papertrading.repository.PaperTradeRepository;
import com.flyingbird.crypto.papertrading.service.TradeEvaluator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Paper-trade evaluation using the latest 1m candle CLOSE (documented choice —
 * matches the project's existing close-based signal logic; intrabar high/low hit
 * detection is a future refinement). Runs even when pattern detection is disabled.
 *
 * <p><b>No-write-on-unchanged (Issue 1).</b> Each OPEN trade is always evaluated, but the
 * entity is mutated and {@code save()} is called ONLY when at least one field actually changes
 * (a TP/SL cross, a discard, or a status change). A trade whose price has not crossed any level
 * is left untouched — no field is set, so Hibernate dirty-checking performs zero UPDATEs for it.
 * The per-run summary logs evaluated / updated / closed / discarded / unchanged-skipped counts.</p>
 *
 * <p><b>Recovery first, then normal evaluation.</b> Before the normal TP/SL ladder, each OPEN
 * trade is checked for (1) invalid required fields → DISCARDED({@code INVALID_PRICE_STATE}) and
 * (2) a recovery gap. Staleness is detected by an <b>in-memory evaluator heartbeat</b>
 * ({@link #lastRunAt}): if the gap between two consecutive evaluator runs exceeds
 * {@code stale-open-trade-minutes}, the evaluator was down/stalled, so every still-OPEN trade is
 * DISCARDED({@code AMBIGUOUS_RECOVERY_STATE} if the latest price is already past TP4/initial-SL,
 * else {@code STALE_OPEN_TRADE}). The heartbeat is used instead of a per-trade {@code lastEvaluatedAt}
 * timestamp on purpose — writing {@code lastEvaluatedAt} every minute would itself dirty every row
 * and defeat Issue 1. {@code lastEvaluatedAt} is therefore updated only when a trade actually
 * changes (its "last state change" time). DISCARDED is terminal, excluded from future evaluation,
 * and does NOT set TP4/STOP_LOSS.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TradeEvaluatorImpl implements TradeEvaluator {

    private final PaperTradeRepository paperTradeRepository;
    private final PaperTradingProperties props;

    /**
     * In-memory heartbeat: timestamp of the previous evaluator run. A gap larger than
     * {@code stale-open-trade-minutes} between two runs means the evaluator was down/stalled
     * (server downtime), so this run is treated as recovery. Volatile + package-private so tests
     * can prime it; the evaluator is driven by a single-threaded executor so runs do not overlap.
     */
    volatile LocalDateTime lastRunAt;

    private enum Outcome { UNCHANGED, UPDATED, CLOSED, DISCARDED }

    @Override
    @Transactional
    public void evaluateOpenTrades(Candle latestOneMinuteCandle) {
        if (latestOneMinuteCandle == null) {
            log.warn("[paper-trading] no latest 1m candle — skipping evaluation");
            return;
        }
        double close = latestOneMinuteCandle.getClose();
        BigDecimal closeBd = BigDecimal.valueOf(close);
        LocalDateTime now = LocalDateTime.now();

        // Heartbeat: compute recovery gap against the previous run, then advance the heartbeat.
        LocalDateTime previousRun = lastRunAt;
        lastRunAt = now;
        boolean recoveryGap = props.getEvaluator().isDiscardStaleOpenTradesEnabled()
                && previousRun != null
                && Duration.between(previousRun, now).toMinutes() > props.getEvaluator().getStaleOpenTradeMinutes();

        List<PaperTrade> open = paperTradeRepository.findByTradeStatus(PaperTradeStatus.OPEN);
        if (open.isEmpty()) {
            return;
        }
        int updated = 0;
        int closed = 0;
        int discarded = 0;
        int unchanged = 0;
        for (PaperTrade t : open) {
            try {
                switch (evaluateOne(t, close, closeBd, now, recoveryGap)) {
                    case UPDATED -> updated++;
                    case CLOSED -> closed++;
                    case DISCARDED -> discarded++;
                    default -> unchanged++;
                }
            } catch (OptimisticLockingFailureException e) {
                log.warn("[paper-trading] trade {} skipped (optimistic lock) — will re-evaluate next candle",
                        t.getTradeId());
            } catch (Exception e) {
                log.error("[paper-trading] error evaluating trade {}: {}", t.getTradeId(), e.getMessage(), e);
            }
        }
        log.debug("[paper-trading] evaluated {} open trade(s) @ close={} | {} updated | {} closed | {} discarded "
                        + "| {} unchanged-skipped (no save)",
                open.size(), close, updated, closed, discarded, unchanged);
    }

    /**
     * Evaluate one trade. Returns {@code UNCHANGED} without mutating or saving the entity when no
     * level is crossed; otherwise mutates the relevant fields once and calls {@code save()} exactly
     * once.
     */
    private Outcome evaluateOne(PaperTrade t, double close, BigDecimal closeBd, LocalDateTime now,
                               boolean recoveryGap) {
        // Recovery 1: invalid trade state → discard (applies on any run).
        if (isInvalidTradeState(t)) {
            discardTrade(t, CloseReason.INVALID_PRICE_STATE, closeBd, now, "invalid trade fields");
            return Outcome.DISCARDED;
        }
        // Recovery 2: evaluator heartbeat gap (downtime / stall) → discard every still-OPEN trade.
        if (recoveryGap) {
            boolean bull = t.getTradeType() == TradeDirection.BULLISH;
            CloseReason reason = isBeyondBounds(bull, close, t)
                    ? CloseReason.AMBIGUOUS_RECOVERY_STATE : CloseReason.STALE_OPEN_TRADE;
            discardTrade(t, reason, closeBd, now, "recovery gap since last evaluator run");
            return Outcome.DISCARDED;
        }

        // Normal evaluation (fresh, valid trade). Mutate ONLY on a real change.
        boolean bull = t.getTradeType() == TradeDirection.BULLISH;
        boolean tp1Before = t.isTp1Achieved();

        // Phase A: initial stop is only active before TP1 protection.
        if (!tp1Before && stopHit(bull, close, t.getStopLoss())) {
            closeTrade(t, CloseReason.STOP_LOSS, closeBd, now); // safeTrade stays false
            log.info("[paper-trading] trade {} CLOSED STOP_LOSS (pre-TP1) @ {}", t.getTradeId(), close);
            paperTradeRepository.save(t);
            return Outcome.CLOSED;
        }

        // Phase B (terminal): TP4 closes the trade.
        if (tpHit(bull, close, t.getTp4())) {
            t.setTp1Achieved(true);
            t.setTp2Achieved(true);
            t.setTp3Achieved(true);
            t.setTp4Achieved(true);
            t.setSafeTrade(true);
            closeTrade(t, CloseReason.TP4, closeBd, now);
            log.info("[paper-trading] trade {} CLOSED TP4 @ {}", t.getTradeId(), close);
            paperTradeRepository.save(t);
            return Outcome.CLOSED;
        }

        // Phase B (non-terminal TP ladder). Each flag flips at most once → no repeated writes.
        boolean changed = false;
        if (!t.isTp1Achieved() && tpHit(bull, close, t.getTp1())) {
            t.setTp1Achieved(true);
            t.setSafeTrade(true);
            t.setStopLoss(t.getTradePrice()); // move stop to breakeven
            changed = true;
        }
        if (!t.isTp2Achieved() && tpHit(bull, close, t.getTp2())) {
            t.setTp2Achieved(true);
            changed = true;
        }
        if (!t.isTp3Achieved() && tpHit(bull, close, t.getTp3())) {
            t.setTp3Achieved(true);
            changed = true;
        }

        // Phase C: after TP1 the breakeven stop (=tradePrice) is active.
        if (t.isTp1Achieved() && stopHit(bull, close, t.getStopLoss())) {
            closeTrade(t, CloseReason.STOP_LOSS, closeBd, now); // safeTrade remains true
            log.info("[paper-trading] trade {} CLOSED STOP_LOSS (breakeven) @ {}", t.getTradeId(), close);
            paperTradeRepository.save(t);
            return Outcome.CLOSED;
        }

        if (changed) {
            t.setLastEvaluatedAt(now); // "last state change" time — set only when something changed
            paperTradeRepository.save(t);
            return Outcome.UPDATED;
        }

        // No level crossed: entity left untouched → no dirty state → no DB write.
        return Outcome.UNCHANGED;
    }

    private void closeTrade(PaperTrade t, CloseReason reason, BigDecimal closePrice, LocalDateTime now) {
        t.setTradeStatus(PaperTradeStatus.CLOSED);
        t.setCloseReason(reason);
        t.setClosePrice(closePrice);
        t.setClosedAt(now);
        t.setLastEvaluatedAt(now);
    }

    /**
     * Mark a trade DISCARDED (terminal recovery state). Preserves TP flags / safeTrade as-is —
     * does NOT set TP4 or STOP_LOSS (those mean a real, normally-evaluated outcome).
     */
    private void discardTrade(PaperTrade t, CloseReason reason, BigDecimal closePrice,
                              LocalDateTime now, String detail) {
        t.setTradeStatus(PaperTradeStatus.DISCARDED);
        t.setCloseReason(reason);
        t.setClosePrice(closePrice);
        t.setClosedAt(now);
        t.setLastEvaluatedAt(now);
        paperTradeRepository.save(t);
        log.info("[paper-trading] Discarded paper trade tradeId={} timeframe={} pattern={} reason={} latestClose={} ({})",
                t.getTradeId(), t.getTimeframe().getCode(), t.getPatternName(), reason, closePrice, detail);
    }

    /** Required fields present + stop on the correct side + TP ladder in the correct direction. */
    private boolean isInvalidTradeState(PaperTrade t) {
        if (t.getTradePrice() == null || t.getTp1() == null || t.getTp2() == null
                || t.getTp3() == null || t.getTp4() == null) {
            return true;
        }
        BigDecimal slBd = t.getInitialStopLoss() != null ? t.getInitialStopLoss() : t.getStopLoss();
        if (slBd == null) {
            return true;
        }
        double entry = t.getTradePrice().doubleValue();
        double sl = slBd.doubleValue();
        double tp1 = t.getTp1().doubleValue();
        double tp2 = t.getTp2().doubleValue();
        double tp3 = t.getTp3().doubleValue();
        double tp4 = t.getTp4().doubleValue();
        if (t.getTradeType() == TradeDirection.BULLISH) {
            if (sl >= entry) {
                return true; // stop must be below entry
            }
            return !(tp1 > entry && tp2 > tp1 && tp3 > tp2 && tp4 > tp3);
        } else {
            if (sl <= entry) {
                return true; // stop must be above entry
            }
            return !(tp1 < entry && tp2 < tp1 && tp3 < tp2 && tp4 < tp3);
        }
    }

    /** Latest price already past TP4 or the initial stop (ambiguous: outcome can't be inferred reliably). */
    private boolean isBeyondBounds(boolean bull, double close, PaperTrade t) {
        BigDecimal slBd = t.getInitialStopLoss() != null ? t.getInitialStopLoss() : t.getStopLoss();
        Double tp4 = t.getTp4() != null ? t.getTp4().doubleValue() : null;
        double sl = slBd != null ? slBd.doubleValue() : (bull ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
        if (bull) {
            return (tp4 != null && close >= tp4) || close <= sl;
        }
        return (tp4 != null && close <= tp4) || close >= sl;
    }

    /** TP reached: bull → close >= level; bear → close <= level. */
    private boolean tpHit(boolean bull, double close, BigDecimal level) {
        if (level == null) {
            return false;
        }
        double l = level.doubleValue();
        return bull ? close >= l : close <= l;
    }

    /** Stop reached: bull → close <= level; bear → close >= level. */
    private boolean stopHit(boolean bull, double close, BigDecimal level) {
        if (level == null) {
            return false;
        }
        double l = level.doubleValue();
        return bull ? close <= l : close >= l;
    }
}
