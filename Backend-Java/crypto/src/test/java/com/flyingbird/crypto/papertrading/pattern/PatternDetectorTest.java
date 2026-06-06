package com.flyingbird.crypto.papertrading.pattern;

import com.flyingbird.crypto.marketdata.model.Candle;
import com.flyingbird.crypto.papertrading.config.PaperTradingProperties;
import com.flyingbird.crypto.papertrading.dto.PatternDetectionResultDto;
import com.flyingbird.crypto.papertrading.enums.ChartPatternName;
import com.flyingbird.crypto.papertrading.enums.TradeDirection;
import com.flyingbird.crypto.papertrading.util.AtrUtils;
import com.flyingbird.crypto.scheduler.common.Timeframe;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the (pure) pattern detector using synthetic candle shapes.
 * Calls the package-private detectors directly so the math is exercised without
 * Spring or a database.
 */
class PatternDetectorTest {

    private final PatternDetectorImpl detector = new PatternDetectorImpl(new PaperTradingProperties());

    private static Candle candle(int idx, double close) {
        Candle c = new Candle();
        // deterministic, parseable time string "yyyyMMdd_HHmmss"
        int minute = idx % 60;
        int hour = (idx / 60) % 24;
        c.setTime(String.format("20260101_%02d%02d00", hour, minute));
        c.setOpen(close);
        c.setHigh(close + 1);
        c.setLow(close - 1);
        c.setClose(close);
        c.setVolume(100);
        return c;
    }

    /** Up → peak1 → trough → peak2(equal) → breakdown below neckline. */
    private List<Candle> doubleTopShape() {
        double[] closes = new double[50];
        for (int i = 0; i <= 22; i++) closes[i] = 100 + i * (30.0 / 22);   // rise to ~130 (peak1 @22)
        for (int i = 23; i <= 31; i++) closes[i] = 130 - (i - 22) * (13.0 / 9);  // fall to ~117 (trough @31)
        for (int i = 32; i <= 39; i++) closes[i] = 117 + (i - 31) * (13.0 / 8);  // rise to ~130 (peak2 @39)
        for (int i = 40; i <= 49; i++) closes[i] = 130 - (i - 39) * (18.0 / 10); // breakdown to ~112
        List<Candle> list = new ArrayList<>();
        for (int i = 0; i < closes.length; i++) list.add(candle(i, closes[i]));
        return list;
    }

    /** Down → trough1 → peak → trough2(equal) → breakout above neckline. */
    private List<Candle> doubleBottomShape() {
        double[] closes = new double[50];
        for (int i = 0; i <= 22; i++) closes[i] = 130 - i * (30.0 / 22);   // fall to ~100 (trough1 @22)
        for (int i = 23; i <= 31; i++) closes[i] = 100 + (i - 22) * (13.0 / 9);  // rise to ~113 (peak @31)
        for (int i = 32; i <= 39; i++) closes[i] = 113 - (i - 31) * (13.0 / 8);  // fall to ~100 (trough2 @39)
        for (int i = 40; i <= 49; i++) closes[i] = 100 + (i - 39) * (18.0 / 10); // breakout to ~118
        List<Candle> list = new ArrayList<>();
        for (int i = 0; i < closes.length; i++) list.add(candle(i, closes[i]));
        return list;
    }

    @Test
    void detectsDoubleTop() {
        List<Candle> c = doubleTopShape();
        double atr = AtrUtils.atr(c, 14);
        double tol = AtrUtils.tolerance(atr, c.get(c.size() - 1).getClose());

        Optional<PatternDetectionResultDto> r = detector.detectDoubleTop(c, Timeframe.FIFTEEN_MINUTE, atr, tol, 3, 3);

        assertThat(r).isPresent();
        PatternDetectionResultDto d = r.get();
        assertThat(d.getPatternName()).isEqualTo(ChartPatternName.DOUBLE_TOP);
        assertThat(d.getTradeDirection()).isEqualTo(TradeDirection.BEARISH);
        // bearish: SL above entry, TP1 below entry
        assertThat(d.getStopLoss().doubleValue()).isGreaterThan(d.getEntryPrice().doubleValue());
        assertThat(d.getTp1().doubleValue()).isLessThan(d.getEntryPrice().doubleValue());
        assertThat(d.getRiskPerUnit().doubleValue()).isGreaterThan(0);
    }

    @Test
    void detectsDoubleBottom() {
        List<Candle> c = doubleBottomShape();
        double atr = AtrUtils.atr(c, 14);
        double tol = AtrUtils.tolerance(atr, c.get(c.size() - 1).getClose());

        Optional<PatternDetectionResultDto> r = detector.detectDoubleBottom(c, Timeframe.FIFTEEN_MINUTE, atr, tol, 3, 3);

        assertThat(r).isPresent();
        PatternDetectionResultDto d = r.get();
        assertThat(d.getPatternName()).isEqualTo(ChartPatternName.DOUBLE_BOTTOM);
        assertThat(d.getTradeDirection()).isEqualTo(TradeDirection.BULLISH);
        // bullish: SL below entry, TP1 above entry
        assertThat(d.getStopLoss().doubleValue()).isLessThan(d.getEntryPrice().doubleValue());
        assertThat(d.getTp1().doubleValue()).isGreaterThan(d.getEntryPrice().doubleValue());
        assertThat(d.getRiskPerUnit().doubleValue()).isGreaterThan(0);
    }

    @Test
    void noPatternOnInsufficientCandles() {
        List<Candle> few = new ArrayList<>();
        for (int i = 0; i < 10; i++) few.add(candle(i, 100 + i));
        assertThat(detector.detectPatterns(few, Timeframe.ONE_MINUTE)).isEmpty();
    }
}
