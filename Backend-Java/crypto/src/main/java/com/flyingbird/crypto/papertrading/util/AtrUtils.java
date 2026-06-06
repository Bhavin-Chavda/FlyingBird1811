package com.flyingbird.crypto.papertrading.util;

import com.flyingbird.crypto.marketdata.model.Candle;

import java.util.List;

/**
 * Average True Range (ATR) — stateless. Used as the SL/breakout buffer.
 */
public final class AtrUtils {

    private AtrUtils() {
    }

    /**
     * ATR over the last {@code period} candles (simple average of true ranges).
     * Returns 0 if there are not enough candles.
     */
    public static double atr(List<Candle> candles, int period) {
        int n = candles.size();
        if (n < 2 || period < 1) {
            return 0;
        }
        int start = Math.max(1, n - period);
        double sum = 0;
        int count = 0;
        for (int i = start; i < n; i++) {
            Candle cur = candles.get(i);
            Candle prev = candles.get(i - 1);
            double tr = Math.max(
                    cur.getHigh() - cur.getLow(),
                    Math.max(Math.abs(cur.getHigh() - prev.getClose()),
                            Math.abs(cur.getLow() - prev.getClose())));
            sum += tr;
            count++;
        }
        return count == 0 ? 0 : sum / count;
    }

    /**
     * Dynamic tolerance for "equal" highs/lows:
     * {@code max(ATR * 0.75, latestClose * 0.003)}.
     */
    public static double tolerance(double atr, double latestClose) {
        return Math.max(atr * 0.75, latestClose * 0.003);
    }
}
