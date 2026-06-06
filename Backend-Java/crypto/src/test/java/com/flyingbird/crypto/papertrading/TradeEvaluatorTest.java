package com.flyingbird.crypto.papertrading;

import com.flyingbird.crypto.marketdata.model.Candle;
import com.flyingbird.crypto.papertrading.config.PaperTradingProperties;
import com.flyingbird.crypto.papertrading.entity.PaperTrade;
import com.flyingbird.crypto.papertrading.enums.ChartPatternName;
import com.flyingbird.crypto.papertrading.enums.CloseReason;
import com.flyingbird.crypto.papertrading.enums.PaperTradeStatus;
import com.flyingbird.crypto.papertrading.enums.TradeDirection;
import com.flyingbird.crypto.papertrading.repository.PaperTradeRepository;
import com.flyingbird.crypto.papertrading.service.impl.TradeEvaluatorImpl;
import com.flyingbird.crypto.scheduler.common.Timeframe;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TradeEvaluatorTest {

    private final PaperTradeRepository repo = mock(PaperTradeRepository.class);
    private final PaperTradingProperties props = new PaperTradingProperties();
    private final TradeEvaluatorImpl evaluator = new TradeEvaluatorImpl(repo, props);

    private static Candle candleAtClose(double close) {
        Candle c = new Candle();
        c.setTime("20260101_000000");
        c.setOpen(close);
        c.setHigh(close + 1);
        c.setLow(close - 1);
        c.setClose(close);
        c.setVolume(1);
        return c;
    }

    private PaperTrade bullish() {
        return PaperTrade.builder()
                .tradeId(1L).timeframe(Timeframe.ONE_MINUTE).tradeType(TradeDirection.BULLISH)
                .tradeStatus(PaperTradeStatus.OPEN).patternName(ChartPatternName.DOUBLE_BOTTOM)
                .candleTime(LocalDateTime.now())
                .tradePrice(bd(100)).stopLoss(bd(90)).initialStopLoss(bd(90))
                .tp1(bd(110)).tp2(bd(120)).tp3(bd(130)).tp4(bd(140))
                .build();
    }

    private PaperTrade bearish() {
        return PaperTrade.builder()
                .tradeId(2L).timeframe(Timeframe.ONE_MINUTE).tradeType(TradeDirection.BEARISH)
                .tradeStatus(PaperTradeStatus.OPEN).patternName(ChartPatternName.DOUBLE_TOP)
                .candleTime(LocalDateTime.now())
                .tradePrice(bd(100)).stopLoss(bd(110)).initialStopLoss(bd(110))
                .tp1(bd(90)).tp2(bd(80)).tp3(bd(70)).tp4(bd(60))
                .build();
    }

    private void evaluate(PaperTrade trade, double close) {
        when(repo.findByTradeStatus(PaperTradeStatus.OPEN)).thenReturn(List.of(trade));
        when(repo.save(any(PaperTrade.class))).thenAnswer(i -> i.getArgument(0));
        evaluator.evaluateOpenTrades(candleAtClose(close));
    }

    @Test
    void bullishTp1MovesStopToBreakevenAndStaysOpen() {
        PaperTrade t = bullish();
        evaluate(t, 110);
        assertThat(t.isTp1Achieved()).isTrue();
        assertThat(t.isSafeTrade()).isTrue();
        assertThat(t.getStopLoss()).isEqualByComparingTo(bd(100)); // moved to entry
        assertThat(t.getTradeStatus()).isEqualTo(PaperTradeStatus.OPEN);
    }

    @Test
    void bullishTp4ClosesTrade() {
        PaperTrade t = bullish();
        evaluate(t, 140);
        assertThat(t.isTp4Achieved()).isTrue();
        assertThat(t.getTradeStatus()).isEqualTo(PaperTradeStatus.CLOSED);
        assertThat(t.getCloseReason()).isEqualTo(CloseReason.TP4);
    }

    @Test
    void bullishStopBeforeTp1ClosesUnsafe() {
        PaperTrade t = bullish();
        evaluate(t, 85);
        assertThat(t.getTradeStatus()).isEqualTo(PaperTradeStatus.CLOSED);
        assertThat(t.getCloseReason()).isEqualTo(CloseReason.STOP_LOSS);
        assertThat(t.isSafeTrade()).isFalse();
    }

    @Test
    void bearishTp1MovesStopToBreakevenAndStaysOpen() {
        PaperTrade t = bearish();
        evaluate(t, 90);
        assertThat(t.isTp1Achieved()).isTrue();
        assertThat(t.isSafeTrade()).isTrue();
        assertThat(t.getStopLoss()).isEqualByComparingTo(bd(100));
        assertThat(t.getTradeStatus()).isEqualTo(PaperTradeStatus.OPEN);
    }

    @Test
    void bearishTp4ClosesTrade() {
        PaperTrade t = bearish();
        evaluate(t, 60);
        assertThat(t.isTp4Achieved()).isTrue();
        assertThat(t.getTradeStatus()).isEqualTo(PaperTradeStatus.CLOSED);
        assertThat(t.getCloseReason()).isEqualTo(CloseReason.TP4);
    }

    @Test
    void bearishStopBeforeTp1ClosesUnsafe() {
        PaperTrade t = bearish();
        evaluate(t, 115);
        assertThat(t.getTradeStatus()).isEqualTo(PaperTradeStatus.CLOSED);
        assertThat(t.getCloseReason()).isEqualTo(CloseReason.STOP_LOSS);
        assertThat(t.isSafeTrade()).isFalse();
    }

    // ─── No-save-on-unchanged (Issue 1) ───────────────────────────────

    @Test
    void unchangedTradeIsNotSaved() {
        PaperTrade t = bullish(); // entry 100, SL 90, tp1 110
        when(repo.findByTradeStatus(PaperTradeStatus.OPEN)).thenReturn(List.of(t));
        evaluator.evaluateOpenTrades(candleAtClose(100)); // no level crossed
        assertThat(t.getTradeStatus()).isEqualTo(PaperTradeStatus.OPEN);
        verify(repo, never()).save(any(PaperTrade.class)); // nothing changed → no write
    }

    @Test
    void tp1AlreadyAchievedWithNoNewLevelIsNotSaved() {
        PaperTrade t = bullish();
        t.setTp1Achieved(true);
        t.setSafeTrade(true);
        t.setStopLoss(bd(100)); // breakeven already in place from a previous run
        when(repo.findByTradeStatus(PaperTradeStatus.OPEN)).thenReturn(List.of(t));
        evaluator.evaluateOpenTrades(candleAtClose(105)); // between breakeven(100) and tp2(120) → no change
        assertThat(t.getTradeStatus()).isEqualTo(PaperTradeStatus.OPEN);
        assertThat(t.isTp2Achieved()).isFalse();
        verify(repo, never()).save(any(PaperTrade.class));
    }

    @Test
    void tp1HitSavesExactlyOnce() {
        PaperTrade t = bullish();
        evaluate(t, 110); // crosses tp1
        assertThat(t.isTp1Achieved()).isTrue();
        verify(repo).save(t); // changed → saved once
    }

    // ─── Recovery / discard ───────────────────────────────────────────

    @Test
    void invalidTradeStateIsDiscarded() {
        PaperTrade t = bullish();
        t.setInitialStopLoss(bd(105)); // stop above entry for a bullish trade → invalid
        t.setStopLoss(bd(105));
        evaluate(t, 100);
        assertThat(t.getTradeStatus()).isEqualTo(PaperTradeStatus.DISCARDED);
        assertThat(t.getCloseReason()).isEqualTo(CloseReason.INVALID_PRICE_STATE);
    }

    @Test
    void freshTradeClosesNormallyNotDiscarded() {
        PaperTrade t = bullish();
        evaluate(t, 140); // first run (no prior heartbeat) → normal evaluation, TP4 close
        assertThat(t.getTradeStatus()).isEqualTo(PaperTradeStatus.CLOSED);
        assertThat(t.getCloseReason()).isEqualTo(CloseReason.TP4);
    }

    private static BigDecimal bd(double v) {
        return BigDecimal.valueOf(v);
    }
}
