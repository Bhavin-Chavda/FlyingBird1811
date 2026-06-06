package com.flyingbird.crypto.papertrading.util;

import com.flyingbird.crypto.marketdata.model.Candle;

import java.util.List;

/**
 * Shared, stateless pattern math: tolerances, extremes, trendline fitting.
 */
public final class PatternMathUtils {

    private PatternMathUtils() {
    }

    /** Absolute difference within the given tolerance. */
    public static boolean within(double a, double b, double tolerance) {
        return Math.abs(a - b) <= tolerance;
    }

    /** Relative difference |a-b| / |b| (b assumed non-zero, positive prices). */
    public static double relDiff(double a, double b) {
        if (b == 0) {
            return Double.MAX_VALUE;
        }
        return Math.abs(a - b) / Math.abs(b);
    }

    /** Highest high over the inclusive index range. */
    public static double highestHigh(List<Candle> c, int from, int to) {
        double m = Double.NEGATIVE_INFINITY;
        for (int i = Math.max(0, from); i <= Math.min(c.size() - 1, to); i++) {
            m = Math.max(m, c.get(i).getHigh());
        }
        return m;
    }

    /** Lowest low over the inclusive index range. */
    public static double lowestLow(List<Candle> c, int from, int to) {
        double m = Double.POSITIVE_INFINITY;
        for (int i = Math.max(0, from); i <= Math.min(c.size() - 1, to); i++) {
            m = Math.min(m, c.get(i).getLow());
        }
        return m;
    }

    /** Value on the line through (x1,y1)-(x2,y2) evaluated at x. */
    public static double lineValueAt(double x1, double y1, double x2, double y2, double x) {
        if (x2 == x1) {
            return y1;
        }
        double slope = (y2 - y1) / (x2 - x1);
        return y1 + slope * (x - x1);
    }

    /** Least-squares slope of y vs x (x = index). Returns 0 if fewer than 2 points. */
    public static double slope(double[] xs, double[] ys) {
        int n = Math.min(xs.length, ys.length);
        if (n < 2) {
            return 0;
        }
        double sx = 0, sy = 0, sxx = 0, sxy = 0;
        for (int i = 0; i < n; i++) {
            sx += xs[i];
            sy += ys[i];
            sxx += xs[i] * xs[i];
            sxy += xs[i] * ys[i];
        }
        double denom = n * sxx - sx * sx;
        if (denom == 0) {
            return 0;
        }
        return (n * sxy - sx * sy) / denom;
    }
}
