package com.flyingbird.crypto.papertrading.pattern;

import com.flyingbird.crypto.marketdata.model.Candle;
import com.flyingbird.crypto.papertrading.config.PaperTradingProperties;
import com.flyingbird.crypto.papertrading.dto.PatternDetectionResultDto;
import com.flyingbird.crypto.papertrading.enums.ChartPatternName;
import com.flyingbird.crypto.papertrading.enums.TradeDirection;
import com.flyingbird.crypto.papertrading.util.AtrUtils;
import com.flyingbird.crypto.papertrading.util.CandleTimeUtils;
import com.flyingbird.crypto.papertrading.util.PatternMathUtils;
import com.flyingbird.crypto.papertrading.util.RiskRewardUtils;
import com.flyingbird.crypto.papertrading.util.SwingPointUtils;
import com.flyingbird.crypto.scheduler.common.Timeframe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Phase-1 chart-pattern detectors. Each method uses explicit, measurable rules
 * (pivots, ATR-scaled tolerance, prior-trend, latest-close breakout confirmation)
 * and produces a trade plan via {@link RiskRewardUtils}. Pure / thread-safe.
 *
 * <p>NOT financial advice — every pattern is a hypothesis and must be backtested
 * and paper-traded (which is exactly what this feature does) before any trust.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PatternDetectorImpl implements PatternDetector {

    private static final String VERSION = "v1";
    private static final int MIN_CANDLES = 40;
    private static final double CONFIDENCE_BASE = 0.62;

    private final PaperTradingProperties props;

    @Override
    public List<PatternDetectionResultDto> detectPatterns(List<Candle> candlesIn, Timeframe timeframe) {
        List<PatternDetectionResultDto> out = new ArrayList<>();
        if (candlesIn == null || candlesIn.size() < MIN_CANDLES) {
            return out;
        }
        List<Candle> c = List.copyOf(candlesIn); // immutable defensive copy
        PaperTradingProperties.PatternDetector cfg = props.getPatternDetector();
        int left = cfg.getPivotLeft();
        int right = cfg.getPivotRight();
        double atr = AtrUtils.atr(c, cfg.getAtrPeriod());
        if (atr <= 0) {
            return out;
        }
        double latestClose = c.get(c.size() - 1).getClose();
        double tol = AtrUtils.tolerance(atr, latestClose);

        addIfPresent(out, detectDoubleTop(c, timeframe, atr, tol, left, right));
        addIfPresent(out, detectDoubleBottom(c, timeframe, atr, tol, left, right));
        addIfPresent(out, detectHeadAndShoulders(c, timeframe, atr, tol, left, right));
        addIfPresent(out, detectInverseHeadAndShoulders(c, timeframe, atr, tol, left, right));
        addIfPresent(out, detectAscendingTriangle(c, timeframe, atr, tol, left, right));
        addIfPresent(out, detectDescendingTriangle(c, timeframe, atr, tol, left, right));
        addIfPresent(out, detectBullFlag(c, timeframe, atr));
        addIfPresent(out, detectBearFlag(c, timeframe, atr));
        addIfPresent(out, detectRisingWedgeBreakdown(c, timeframe, atr, left, right));
        addIfPresent(out, detectFallingWedgeBreakout(c, timeframe, atr, left, right));
        addIfPresent(out, detectRectangleBreakoutUp(c, timeframe, atr, tol, left, right));
        addIfPresent(out, detectRectangleBreakdownDown(c, timeframe, atr, tol, left, right));

        double minConf = cfg.getMinConfidence();
        out.removeIf(r -> r.getConfidenceScore() == null || r.getConfidenceScore().doubleValue() < minConf);
        return out;
    }

    // ───────────────────────── Reversal: tops/bottoms ─────────────────────────

    Optional<PatternDetectionResultDto> detectDoubleTop(List<Candle> c, Timeframe tf, double atr, double tol,
                                                        int left, int right) {
        int n = c.size();
        int window = Math.min(120, n);
        List<Integer> highs = recentSwingHighs(c, window, left, right);
        if (highs.size() < 2) {
            return Optional.empty();
        }
        int i2 = highs.get(highs.size() - 1);
        int i1 = highs.get(highs.size() - 2);
        double h1 = c.get(i1).getHigh();
        double h2 = c.get(i2).getHigh();
        if (!PatternMathUtils.within(h1, h2, tol)) {
            return Optional.empty();
        }
        double neckline = PatternMathUtils.lowestLow(c, i1, i2);
        double avgHigh = (h1 + h2) / 2.0;
        if (avgHigh - neckline < 1.5 * atr) { // meaningful trough
            return Optional.empty();
        }
        if (!priorUptrend(c, i1)) {
            return Optional.empty();
        }
        double latestClose = c.get(n - 1).getClose();
        if (latestClose >= neckline) { // need breakdown below neckline
            return Optional.empty();
        }
        double sl = Math.max(h1, h2) + atr;
        double conf = confidence((neckline - latestClose) / atr);
        return build(c, tf, ChartPatternName.DOUBLE_TOP, TradeDirection.BEARISH, latestClose, sl, atr,
                neckline, conf, "Double top: two equal highs, close below neckline " + fmt(neckline), i1);
    }

    Optional<PatternDetectionResultDto> detectDoubleBottom(List<Candle> c, Timeframe tf, double atr, double tol,
                                                           int left, int right) {
        int n = c.size();
        int window = Math.min(120, n);
        List<Integer> lows = recentSwingLows(c, window, left, right);
        if (lows.size() < 2) {
            return Optional.empty();
        }
        int i2 = lows.get(lows.size() - 1);
        int i1 = lows.get(lows.size() - 2);
        double l1 = c.get(i1).getLow();
        double l2 = c.get(i2).getLow();
        if (!PatternMathUtils.within(l1, l2, tol)) {
            return Optional.empty();
        }
        double neckline = PatternMathUtils.highestHigh(c, i1, i2);
        double avgLow = (l1 + l2) / 2.0;
        if (neckline - avgLow < 1.5 * atr) {
            return Optional.empty();
        }
        if (!priorDowntrend(c, i1)) {
            return Optional.empty();
        }
        double latestClose = c.get(n - 1).getClose();
        if (latestClose <= neckline) {
            return Optional.empty();
        }
        double sl = Math.min(l1, l2) - atr;
        double conf = confidence((latestClose - neckline) / atr);
        return build(c, tf, ChartPatternName.DOUBLE_BOTTOM, TradeDirection.BULLISH, latestClose, sl, atr,
                neckline, conf, "Double bottom: two equal lows, close above neckline " + fmt(neckline), i1);
    }

    Optional<PatternDetectionResultDto> detectHeadAndShoulders(List<Candle> c, Timeframe tf, double atr, double tol,
                                                               int left, int right) {
        int n = c.size();
        int window = Math.min(160, n);
        List<Integer> highs = recentSwingHighs(c, window, left, right);
        if (highs.size() < 3) {
            return Optional.empty();
        }
        int ls = highs.get(highs.size() - 3);
        int head = highs.get(highs.size() - 2);
        int rs = highs.get(highs.size() - 1);
        double hLs = c.get(ls).getHigh();
        double hHead = c.get(head).getHigh();
        double hRs = c.get(rs).getHigh();
        if (!(hHead > hLs && hHead > hRs)) {
            return Optional.empty();
        }
        if (!PatternMathUtils.within(hLs, hRs, tol * 1.5)) { // shoulders similar
            return Optional.empty();
        }
        int leftTrough = lowestIdx(c, ls, head);
        int rightTrough = lowestIdx(c, head, rs);
        double necklineAtLast = PatternMathUtils.lineValueAt(leftTrough, c.get(leftTrough).getLow(),
                rightTrough, c.get(rightTrough).getLow(), n - 1);
        if (!priorUptrend(c, ls)) {
            return Optional.empty();
        }
        double latestClose = c.get(n - 1).getClose();
        if (latestClose >= necklineAtLast) {
            return Optional.empty();
        }
        double sl = (props.getPatternDetector().isConservativeStops() ? hHead : hRs) + atr;
        double conf = confidence((necklineAtLast - latestClose) / atr);
        return build(c, tf, ChartPatternName.HEAD_AND_SHOULDERS, TradeDirection.BEARISH, latestClose, sl, atr,
                necklineAtLast, conf, "Head & shoulders: head above shoulders, close below neckline", ls);
    }

    Optional<PatternDetectionResultDto> detectInverseHeadAndShoulders(List<Candle> c, Timeframe tf, double atr,
                                                                      double tol, int left, int right) {
        int n = c.size();
        int window = Math.min(160, n);
        List<Integer> lows = recentSwingLows(c, window, left, right);
        if (lows.size() < 3) {
            return Optional.empty();
        }
        int ls = lows.get(lows.size() - 3);
        int head = lows.get(lows.size() - 2);
        int rs = lows.get(lows.size() - 1);
        double lLs = c.get(ls).getLow();
        double lHead = c.get(head).getLow();
        double lRs = c.get(rs).getLow();
        if (!(lHead < lLs && lHead < lRs)) {
            return Optional.empty();
        }
        if (!PatternMathUtils.within(lLs, lRs, tol * 1.5)) {
            return Optional.empty();
        }
        int leftPeak = highestIdx(c, ls, head);
        int rightPeak = highestIdx(c, head, rs);
        double necklineAtLast = PatternMathUtils.lineValueAt(leftPeak, c.get(leftPeak).getHigh(),
                rightPeak, c.get(rightPeak).getHigh(), n - 1);
        if (!priorDowntrend(c, ls)) {
            return Optional.empty();
        }
        double latestClose = c.get(n - 1).getClose();
        if (latestClose <= necklineAtLast) {
            return Optional.empty();
        }
        double sl = (props.getPatternDetector().isConservativeStops() ? lHead : lRs) - atr;
        double conf = confidence((latestClose - necklineAtLast) / atr);
        return build(c, tf, ChartPatternName.INVERSE_HEAD_AND_SHOULDERS, TradeDirection.BULLISH, latestClose, sl,
                atr, necklineAtLast, conf, "Inverse H&S: head below shoulders, close above neckline", ls);
    }

    // ───────────────────────── Triangles ─────────────────────────

    Optional<PatternDetectionResultDto> detectAscendingTriangle(List<Candle> c, Timeframe tf, double atr, double tol,
                                                                int left, int right) {
        int n = c.size();
        int window = Math.min(150, n);
        List<Integer> highs = recentSwingHighs(c, window, left, right);
        List<Integer> lows = recentSwingLows(c, window, left, right);
        if (highs.size() < 2 || lows.size() < 2) {
            return Optional.empty();
        }
        double resistance = avgFlatLevel(c, highs, tol, true);
        if (Double.isNaN(resistance) || !risingLows(c, lows)) {
            return Optional.empty();
        }
        double latestClose = c.get(n - 1).getClose();
        if (latestClose <= resistance + 0.1 * atr) {
            return Optional.empty();
        }
        double sl = c.get(lows.get(lows.size() - 1)).getLow() - atr;
        double conf = confidence((latestClose - resistance) / atr);
        return build(c, tf, ChartPatternName.ASCENDING_TRIANGLE, TradeDirection.BULLISH, latestClose, sl, atr,
                resistance, conf, "Ascending triangle: flat resistance " + fmt(resistance) + ", rising lows, breakout",
                Math.min(highs.get(0), lows.get(0)));
    }

    Optional<PatternDetectionResultDto> detectDescendingTriangle(List<Candle> c, Timeframe tf, double atr, double tol,
                                                                 int left, int right) {
        int n = c.size();
        int window = Math.min(150, n);
        List<Integer> highs = recentSwingHighs(c, window, left, right);
        List<Integer> lows = recentSwingLows(c, window, left, right);
        if (highs.size() < 2 || lows.size() < 2) {
            return Optional.empty();
        }
        double support = avgFlatLevel(c, lows, tol, false);
        if (Double.isNaN(support) || !fallingHighs(c, highs)) {
            return Optional.empty();
        }
        double latestClose = c.get(n - 1).getClose();
        if (latestClose >= support - 0.1 * atr) {
            return Optional.empty();
        }
        double sl = c.get(highs.get(highs.size() - 1)).getHigh() + atr;
        double conf = confidence((support - latestClose) / atr);
        return build(c, tf, ChartPatternName.DESCENDING_TRIANGLE, TradeDirection.BEARISH, latestClose, sl, atr,
                support, conf, "Descending triangle: flat support " + fmt(support) + ", falling highs, breakdown",
                Math.min(highs.get(0), lows.get(0)));
    }

    // ───────────────────────── Flags ─────────────────────────

    Optional<PatternDetectionResultDto> detectBullFlag(List<Candle> c, Timeframe tf, double atr) {
        int n = c.size();
        int flagLen = 12;
        int impulseLen = 15;
        if (n < flagLen + impulseLen + 2) {
            return Optional.empty();
        }
        int flagStart = n - 1 - flagLen;          // flag region [flagStart, n-2]
        int impulseStart = flagStart - impulseLen;
        double impulseMove = c.get(flagStart).getClose() - c.get(impulseStart).getClose();
        if (impulseMove < 2 * atr) {              // strong up impulse
            return Optional.empty();
        }
        double flagHigh = PatternMathUtils.highestHigh(c, flagStart, n - 2);
        double flagLow = PatternMathUtils.lowestLow(c, flagStart, n - 2);
        if (flagHigh - flagLow >= impulseMove * 0.6) { // consolidation smaller than impulse
            return Optional.empty();
        }
        double latestClose = c.get(n - 1).getClose();
        if (latestClose <= flagHigh) {            // breakout above flag
            return Optional.empty();
        }
        double sl = flagLow - atr;
        double conf = confidence((latestClose - flagHigh) / atr);
        return build(c, tf, ChartPatternName.BULL_FLAG, TradeDirection.BULLISH, latestClose, sl, atr,
                flagHigh, conf, "Bull flag: up impulse + tight consolidation, breakout above " + fmt(flagHigh),
                impulseStart);
    }

    Optional<PatternDetectionResultDto> detectBearFlag(List<Candle> c, Timeframe tf, double atr) {
        int n = c.size();
        int flagLen = 12;
        int impulseLen = 15;
        if (n < flagLen + impulseLen + 2) {
            return Optional.empty();
        }
        int flagStart = n - 1 - flagLen;
        int impulseStart = flagStart - impulseLen;
        double impulseMove = c.get(impulseStart).getClose() - c.get(flagStart).getClose();
        if (impulseMove < 2 * atr) {              // strong down impulse
            return Optional.empty();
        }
        double flagHigh = PatternMathUtils.highestHigh(c, flagStart, n - 2);
        double flagLow = PatternMathUtils.lowestLow(c, flagStart, n - 2);
        if (flagHigh - flagLow >= impulseMove * 0.6) {
            return Optional.empty();
        }
        double latestClose = c.get(n - 1).getClose();
        if (latestClose >= flagLow) {             // breakdown below flag
            return Optional.empty();
        }
        double sl = flagHigh + atr;
        double conf = confidence((flagLow - latestClose) / atr);
        return build(c, tf, ChartPatternName.BEAR_FLAG, TradeDirection.BEARISH, latestClose, sl, atr,
                flagLow, conf, "Bear flag: down impulse + tight consolidation, breakdown below " + fmt(flagLow),
                impulseStart);
    }

    // ───────────────────────── Wedges ─────────────────────────

    Optional<PatternDetectionResultDto> detectRisingWedgeBreakdown(List<Candle> c, Timeframe tf, double atr,
                                                                   int left, int right) {
        int n = c.size();
        int window = Math.min(120, n);
        List<Integer> highs = recentSwingHighs(c, window, left, right);
        List<Integer> lows = recentSwingLows(c, window, left, right);
        if (highs.size() < 3 || lows.size() < 3) {
            return Optional.empty();
        }
        double highSlope = slopeOf(c, highs, true);
        double lowSlope = slopeOf(c, lows, false);
        if (!(highSlope > 0 && lowSlope > 0 && lowSlope > highSlope)) { // both rising, converging
            return Optional.empty();
        }
        int la = lows.get(lows.size() - 2);
        int lb = lows.get(lows.size() - 1);
        double lowerAtLast = PatternMathUtils.lineValueAt(la, c.get(la).getLow(), lb, c.get(lb).getLow(), n - 1);
        double latestClose = c.get(n - 1).getClose();
        if (latestClose >= lowerAtLast) {
            return Optional.empty();
        }
        double sl = c.get(highs.get(highs.size() - 1)).getHigh() + atr;
        double conf = confidence((lowerAtLast - latestClose) / atr);
        return build(c, tf, ChartPatternName.RISING_WEDGE_BREAKDOWN, TradeDirection.BEARISH, latestClose, sl, atr,
                lowerAtLast, conf, "Rising wedge: converging up-slopes, close below lower trendline", lows.get(0));
    }

    Optional<PatternDetectionResultDto> detectFallingWedgeBreakout(List<Candle> c, Timeframe tf, double atr,
                                                                   int left, int right) {
        int n = c.size();
        int window = Math.min(120, n);
        List<Integer> highs = recentSwingHighs(c, window, left, right);
        List<Integer> lows = recentSwingLows(c, window, left, right);
        if (highs.size() < 3 || lows.size() < 3) {
            return Optional.empty();
        }
        double highSlope = slopeOf(c, highs, true);
        double lowSlope = slopeOf(c, lows, false);
        if (!(highSlope < 0 && lowSlope < 0 && highSlope < lowSlope)) { // both falling, converging
            return Optional.empty();
        }
        int ha = highs.get(highs.size() - 2);
        int hb = highs.get(highs.size() - 1);
        double upperAtLast = PatternMathUtils.lineValueAt(ha, c.get(ha).getHigh(), hb, c.get(hb).getHigh(), n - 1);
        double latestClose = c.get(n - 1).getClose();
        if (latestClose <= upperAtLast) {
            return Optional.empty();
        }
        double sl = c.get(lows.get(lows.size() - 1)).getLow() - atr;
        double conf = confidence((latestClose - upperAtLast) / atr);
        return build(c, tf, ChartPatternName.FALLING_WEDGE_BREAKOUT, TradeDirection.BULLISH, latestClose, sl, atr,
                upperAtLast, conf, "Falling wedge: converging down-slopes, close above upper trendline", highs.get(0));
    }

    // ───────────────────────── Rectangles ─────────────────────────

    Optional<PatternDetectionResultDto> detectRectangleBreakoutUp(List<Candle> c, Timeframe tf, double atr, double tol,
                                                                  int left, int right) {
        int n = c.size();
        int window = Math.min(120, n);
        List<Integer> highs = recentSwingHighs(c, window, left, right);
        List<Integer> lows = recentSwingLows(c, window, left, right);
        if (highs.size() < 2 || lows.size() < 2) {
            return Optional.empty();
        }
        double resistance = avgFlatLevel(c, highs, tol, true);
        double support = avgFlatLevel(c, lows, tol, false);
        if (Double.isNaN(resistance) || Double.isNaN(support) || resistance - support < 1.5 * atr) {
            return Optional.empty();
        }
        double latestClose = c.get(n - 1).getClose();
        if (latestClose <= resistance + 0.1 * atr) {
            return Optional.empty();
        }
        double sl = support - atr;
        double conf = confidence((latestClose - resistance) / atr);
        return build(c, tf, ChartPatternName.RECTANGLE_BREAKOUT_UP, TradeDirection.BULLISH, latestClose, sl, atr,
                resistance, conf, "Rectangle range [" + fmt(support) + "," + fmt(resistance) + "] breakout up",
                Math.min(highs.get(0), lows.get(0)));
    }

    Optional<PatternDetectionResultDto> detectRectangleBreakdownDown(List<Candle> c, Timeframe tf, double atr,
                                                                     double tol, int left, int right) {
        int n = c.size();
        int window = Math.min(120, n);
        List<Integer> highs = recentSwingHighs(c, window, left, right);
        List<Integer> lows = recentSwingLows(c, window, left, right);
        if (highs.size() < 2 || lows.size() < 2) {
            return Optional.empty();
        }
        double resistance = avgFlatLevel(c, highs, tol, true);
        double support = avgFlatLevel(c, lows, tol, false);
        if (Double.isNaN(resistance) || Double.isNaN(support) || resistance - support < 1.5 * atr) {
            return Optional.empty();
        }
        double latestClose = c.get(n - 1).getClose();
        if (latestClose >= support - 0.1 * atr) {
            return Optional.empty();
        }
        double sl = resistance + atr;
        double conf = confidence((support - latestClose) / atr);
        return build(c, tf, ChartPatternName.RECTANGLE_BREAKDOWN_DOWN, TradeDirection.BEARISH, latestClose, sl, atr,
                support, conf, "Rectangle range [" + fmt(support) + "," + fmt(resistance) + "] breakdown down",
                Math.min(highs.get(0), lows.get(0)));
    }

    // ───────────────────────── Helpers ─────────────────────────

    private void addIfPresent(List<PatternDetectionResultDto> out, Optional<PatternDetectionResultDto> r) {
        r.ifPresent(out::add);
    }

    private List<Integer> recentSwingHighs(List<Candle> c, int window, int left, int right) {
        int from = c.size() - window;
        List<Integer> all = SwingPointUtils.swingHighs(c, left, right);
        List<Integer> out = new ArrayList<>();
        for (int i : all) {
            if (i >= from) {
                out.add(i);
            }
        }
        return out;
    }

    private List<Integer> recentSwingLows(List<Candle> c, int window, int left, int right) {
        int from = c.size() - window;
        List<Integer> all = SwingPointUtils.swingLows(c, left, right);
        List<Integer> out = new ArrayList<>();
        for (int i : all) {
            if (i >= from) {
                out.add(i);
            }
        }
        return out;
    }

    /** Average of the swing-point values that cluster near the extreme (flat line); NaN if fewer than 2. */
    private double avgFlatLevel(List<Candle> c, List<Integer> idx, double tol, boolean useHigh) {
        double extreme = useHigh ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        for (int i : idx) {
            double v = useHigh ? c.get(i).getHigh() : c.get(i).getLow();
            extreme = useHigh ? Math.max(extreme, v) : Math.min(extreme, v);
        }
        double sum = 0;
        int count = 0;
        for (int i : idx) {
            double v = useHigh ? c.get(i).getHigh() : c.get(i).getLow();
            if (PatternMathUtils.within(v, extreme, tol)) {
                sum += v;
                count++;
            }
        }
        return count >= 2 ? sum / count : Double.NaN;
    }

    private boolean risingLows(List<Candle> c, List<Integer> lows) {
        return slopeOf(c, lows, false) > 0
                && c.get(lows.get(lows.size() - 1)).getLow() > c.get(lows.get(0)).getLow();
    }

    private boolean fallingHighs(List<Candle> c, List<Integer> highs) {
        return slopeOf(c, highs, true) < 0
                && c.get(highs.get(highs.size() - 1)).getHigh() < c.get(highs.get(0)).getHigh();
    }

    private double slopeOf(List<Candle> c, List<Integer> idx, boolean useHigh) {
        double[] xs = new double[idx.size()];
        double[] ys = new double[idx.size()];
        for (int k = 0; k < idx.size(); k++) {
            xs[k] = idx.get(k);
            ys[k] = useHigh ? c.get(idx.get(k)).getHigh() : c.get(idx.get(k)).getLow();
        }
        return PatternMathUtils.slope(xs, ys);
    }

    private int lowestIdx(List<Candle> c, int from, int to) {
        int best = from;
        for (int i = from; i <= to; i++) {
            if (c.get(i).getLow() < c.get(best).getLow()) {
                best = i;
            }
        }
        return best;
    }

    private int highestIdx(List<Candle> c, int from, int to) {
        int best = from;
        for (int i = from; i <= to; i++) {
            if (c.get(i).getHigh() > c.get(best).getHigh()) {
                best = i;
            }
        }
        return best;
    }

    private boolean priorUptrend(List<Candle> c, int beforeIdx) {
        int look = Math.min(20, beforeIdx);
        if (look < 5) {
            return false;
        }
        return c.get(beforeIdx).getClose() > c.get(beforeIdx - look).getClose() * 1.005;
    }

    private boolean priorDowntrend(List<Candle> c, int beforeIdx) {
        int look = Math.min(20, beforeIdx);
        if (look < 5) {
            return false;
        }
        return c.get(beforeIdx).getClose() < c.get(beforeIdx - look).getClose() * 0.995;
    }

    private double confidence(double breakoutStrengthAtr) {
        double bonus = 0.10 * Math.max(0, Math.min(breakoutStrengthAtr, 3.0));
        return Math.min(0.95, CONFIDENCE_BASE + bonus);
    }

    private Optional<PatternDetectionResultDto> build(List<Candle> c, Timeframe tf, ChartPatternName pattern,
                                                      TradeDirection dir, double entry, double sl, double atr,
                                                      double breakoutLevel, double confidence, String reason,
                                                      int startIdx) {
        if (!finite(entry) || !finite(sl) || !finite(breakoutLevel)) {
            return Optional.empty();
        }
        OptionalDouble riskOpt = RiskRewardUtils.risk(dir, entry, sl);
        if (riskOpt.isEmpty()) {
            return Optional.empty();
        }
        double risk = riskOpt.getAsDouble();
        double[] tps = RiskRewardUtils.takeProfits(dir, entry, risk);
        Candle latest = c.get(c.size() - 1);
        int safeStart = Math.max(0, Math.min(startIdx, c.size() - 1));
        return Optional.of(PatternDetectionResultDto.builder()
                .detected(true)
                .timeframe(tf)
                .patternName(pattern)
                .tradeDirection(dir)
                .signalCandleTime(CandleTimeUtils.parse(latest.getTime()))
                .entryPrice(bd(entry))
                .stopLoss(bd(sl))
                .tp1(bd(tps[0])).tp2(bd(tps[1])).tp3(bd(tps[2])).tp4(bd(tps[3]))
                .riskPerUnit(bd(risk))
                .breakoutLevel(bd(breakoutLevel))
                .atrAtDetection(bd(atr))
                .confidenceScore(BigDecimal.valueOf(confidence).setScale(4, RoundingMode.HALF_UP))
                .detectionReason(truncate(reason))
                .candlesAnalyzed((c.size() - 1) - safeStart + 1)
                .patternStartTime(CandleTimeUtils.parse(c.get(safeStart).getTime()))
                .patternEndTime(CandleTimeUtils.parse(latest.getTime()))
                .build());
    }

    private static boolean finite(double v) {
        return !Double.isNaN(v) && !Double.isInfinite(v);
    }

    private static BigDecimal bd(double v) {
        return BigDecimal.valueOf(v).setScale(8, RoundingMode.HALF_UP);
    }

    private static String fmt(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static String truncate(String s) {
        return s != null && s.length() > 480 ? s.substring(0, 480) : s;
    }
}
