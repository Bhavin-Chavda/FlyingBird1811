package com.flyingbird.crypto.papertrading.util;

import com.flyingbird.crypto.papertrading.enums.TradeDirection;

import java.util.OptionalDouble;

/**
 * Risk and take-profit math (R multiples). Stateless.
 *
 * <p>Bullish: risk = entry - sl; TPn = entry + n*risk.
 * Bearish: risk = sl - entry; TPn = entry - n*risk. Risk must be &gt; 0 and the
 * stop must be on the correct side of entry.</p>
 */
public final class RiskRewardUtils {

    private RiskRewardUtils() {
    }

    /** Stop must be below entry for bullish, above entry for bearish. */
    public static boolean validStopSide(TradeDirection direction, double entry, double stopLoss) {
        return direction == TradeDirection.BULLISH ? stopLoss < entry : stopLoss > entry;
    }

    /** Positive risk-per-unit, or empty if invalid (zero/negative or wrong-side stop). */
    public static OptionalDouble risk(TradeDirection direction, double entry, double stopLoss) {
        if (!validStopSide(direction, entry, stopLoss)) {
            return OptionalDouble.empty();
        }
        double r = direction == TradeDirection.BULLISH ? entry - stopLoss : stopLoss - entry;
        return r > 0 ? OptionalDouble.of(r) : OptionalDouble.empty();
    }

    /** TP1..TP4 (index 0..3) at 1R..4R in the trade direction. */
    public static double[] takeProfits(TradeDirection direction, double entry, double risk) {
        int sign = direction == TradeDirection.BULLISH ? 1 : -1;
        return new double[]{
                entry + sign * risk,
                entry + sign * 2 * risk,
                entry + sign * 3 * risk,
                entry + sign * 4 * risk
        };
    }
}
