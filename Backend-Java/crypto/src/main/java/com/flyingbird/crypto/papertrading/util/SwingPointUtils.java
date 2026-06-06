package com.flyingbird.crypto.papertrading.util;

import com.flyingbird.crypto.marketdata.model.Candle;

import java.util.ArrayList;
import java.util.List;

/**
 * Swing-high / swing-low (pivot) detection. Stateless and thread-safe.
 *
 * <p>A candle at index {@code i} is a swing high if its {@code high} is the maximum
 * across the window {@code [i-left, i+right]}; symmetrically for swing low using
 * {@code low}. The right window means a pivot is only confirmed once enough later
 * candles exist (no look-ahead beyond the supplied, already-closed candles).</p>
 */
public final class SwingPointUtils {

    private SwingPointUtils() {
    }

    public static boolean isSwingHigh(List<Candle> candles, int i, int left, int right) {
        if (i - left < 0 || i + right >= candles.size()) {
            return false;
        }
        double h = candles.get(i).getHigh();
        for (int j = i - left; j <= i + right; j++) {
            if (j != i && candles.get(j).getHigh() > h) {
                return false;
            }
        }
        return true;
    }

    public static boolean isSwingLow(List<Candle> candles, int i, int left, int right) {
        if (i - left < 0 || i + right >= candles.size()) {
            return false;
        }
        double l = candles.get(i).getLow();
        for (int j = i - left; j <= i + right; j++) {
            if (j != i && candles.get(j).getLow() < l) {
                return false;
            }
        }
        return true;
    }

    /** Indices of swing highs in ascending order. */
    public static List<Integer> swingHighs(List<Candle> candles, int left, int right) {
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < candles.size(); i++) {
            if (isSwingHigh(candles, i, left, right)) {
                out.add(i);
            }
        }
        return out;
    }

    /** Indices of swing lows in ascending order. */
    public static List<Integer> swingLows(List<Candle> candles, int left, int right) {
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < candles.size(); i++) {
            if (isSwingLow(candles, i, left, right)) {
                out.add(i);
            }
        }
        return out;
    }
}
