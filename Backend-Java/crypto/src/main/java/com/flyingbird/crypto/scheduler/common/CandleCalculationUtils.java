package com.flyingbird.crypto.scheduler.common;

import com.flyingbird.crypto.marketdata.model.Candle;
import com.flyingbird.crypto.marketdata.model.OrderRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

/**
 * Candle Calculation Utilities
 *
 * The ONLY shared scheduler logic — pure, stateless EMA + crossover math
 * (TradingView-style). Every method is static and side-effect-free except where
 * it mutates a buffer passed in by the caller; the caller is responsible for
 * holding the appropriate lock around any buffer mutation. Safe to call from
 * multiple job threads concurrently (no shared mutable state held here).
 */
public final class CandleCalculationUtils {

    private CandleCalculationUtils() {
    }

    /** EMA periods computed for every candle. */
    public static final int[] EMA_PERIODS = {21, 30, 35, 40, 45, 50, 60, 200};

    // ------------------------------------------------------------------ EMA

    /** Fill all configured EMA periods in-place across a chronological list. */
    public static void seedAndFill(List<Candle> candles) {
        seedAndFill(candles, EMA_PERIODS);
    }

    /** Fill the given EMA periods in-place across a chronological list. */
    public static void seedAndFill(List<Candle> candles, int[] periods) {
        if (candles == null || candles.isEmpty()) {
            return;
        }
        int n = candles.size();
        double[] closes = new double[n];
        for (int i = 0; i < n; i++) {
            closes[i] = candles.get(i).getClose();
        }
        int[] sorted = periods.clone();
        Arrays.sort(sorted);

        for (int period : sorted) {
            if (n < period) {
                continue;
            }
            double k = 2.0 / (period + 1);
            for (int i = 0; i < period - 1; i++) {
                candles.get(i).setEma(period, null);
            }
            double sum = 0;
            for (int i = 0; i < period; i++) {
                sum += closes[i];
            }
            double seedSma = sum / period;
            candles.get(period - 1).setEma(period, seedSma);

            double prevEma = seedSma;
            for (int i = period; i < n; i++) {
                prevEma = (closes[i] - prevEma) * k + prevEma;
                candles.get(i).setEma(period, prevEma);
            }
        }
    }

    /** Incremental EMA step. */
    public static double nextEma(double prevEma, double close, int period) {
        double k = 2.0 / (period + 1);
        return (close - prevEma) * k + prevEma;
    }

    /**
     * Append the latest closed candle to {@code buffer} with incrementally
     * computed EMAs (dedup by candle time; enforce {@code maxLen}).
     * The CALLER must hold the buffer's write lock — this method does no locking.
     *
     * @return true if a new candle was appended; false if it was a duplicate.
     */
    public static boolean appendWithEma(Deque<Candle> buffer, Candle latest, int maxLen) {
        if (buffer.isEmpty()) {
            buffer.addLast(latest);
            return true;
        }
        Candle last = buffer.peekLast();
        if (latest.getTime().equals(last.getTime())) {
            return false;
        }
        if (buffer.size() >= maxLen) {
            buffer.pollFirst();
        }

        List<Candle> tmp = null;
        Candle prevLast = buffer.peekLast();
        for (int period : EMA_PERIODS) {
            Double prev = prevLast.getEma(period);
            if (prev != null) {
                latest.setEma(period, nextEma(prev, latest.getClose(), period));
            } else {
                if (tmp == null) {
                    tmp = new ArrayList<>(buffer);
                }
                if (tmp.size() >= period) {
                    seedAndFill(tmp, new int[]{period});
                    Double prevSeeded = tmp.get(tmp.size() - 1).getEma(period);
                    latest.setEma(period,
                            prevSeeded != null ? nextEma(prevSeeded, latest.getClose(), period) : null);
                } else {
                    latest.setEma(period, null);
                }
            }
        }
        buffer.addLast(latest);
        return true;
    }

    // -------------------------------------------------------------- Signals

    /**
     * Evaluate the EMA-stack crossover signal for the latest candle (stateless).
     * Returns BULLISH / BEARISH / NEUTRAL. NEUTRAL if EMAs are not yet available.
     */
    public static String evaluateSignal(List<Candle> candles) {
        if (candles == null || candles.isEmpty()) {
            return SchedulerConstants.SIGNAL_NEUTRAL;
        }
        Candle c = candles.get(candles.size() - 1);
        Double e30 = c.getEma(30), e35 = c.getEma(35), e40 = c.getEma(40),
                e45 = c.getEma(45), e50 = c.getEma(50), e60 = c.getEma(60);
        if (anyNull(e30, e35, e40, e45, e50, e60)) {
            return SchedulerConstants.SIGNAL_NEUTRAL;
        }
        if (e30 >= e35 && e35 >= e40 && e40 >= e45 && e45 >= e50 && e50 >= e60) {
            return SchedulerConstants.SIGNAL_BULLISH;
        }
        if (e30 <= e35 && e35 <= e40 && e40 <= e45 && e45 <= e50 && e50 <= e60) {
            return SchedulerConstants.SIGNAL_BEARISH;
        }
        return SchedulerConstants.SIGNAL_NEUTRAL;
    }

    /** Build a bracket market order for a signal (stateless). */
    public static OrderRequest buildOrder(String resolution, String productId, String side,
                                          double slPrice, double tpPrice) {
        return OrderRequest.builder()
                .productId(productId)
                .size(2)
                .reduceOnly(false)
                .orderType("market_order")
                .clientOrderId(resolution + "-" + (System.currentTimeMillis() / 1000))
                .side(side)
                .bracketStopLossPrice(String.valueOf(slPrice))
                .bracketTakeProfitPrice(String.valueOf(tpPrice))
                .build();
    }

    public static double min3(double a, double b, double c) {
        return Math.min(a, Math.min(b, c));
    }

    public static double max3(double a, double b, double c) {
        return Math.max(a, Math.max(b, c));
    }

    private static boolean anyNull(Double... values) {
        for (Double v : values) {
            if (v == null) {
                return true;
            }
        }
        return false;
    }
}
