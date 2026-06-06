package com.flyingbird.crypto.papertrading.service.impl;

import com.flyingbird.crypto.marketdata.model.Candle;
import com.flyingbird.crypto.papertrading.config.PaperTradingProperties;
import com.flyingbird.crypto.papertrading.entity.PaperTrade;
import com.flyingbird.crypto.papertrading.enums.ChartPatternName;
import com.flyingbird.crypto.papertrading.enums.CloseReason;
import com.flyingbird.crypto.papertrading.enums.PaperTradeStatus;
import com.flyingbird.crypto.papertrading.enums.TradeDirection;
import com.flyingbird.crypto.papertrading.repository.PaperTradeRepository;
import com.flyingbird.crypto.scheduler.common.Timeframe;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Recovery-gap tests. The evaluator detects server downtime via an in-memory heartbeat
 * ({@code lastRunAt}); these tests prime it directly (package-private field), which is why
 * they live in the impl package rather than alongside {@code TradeEvaluatorTest}.
 */
class TradeEvaluatorRecoveryTest {

    private final PaperTradeRepository repo = mock(PaperTradeRepository.class);
    private final PaperTradingProperties props = new PaperTradingProperties();
    private final TradeEvaluatorImpl evaluator = new TradeEvaluatorImpl(repo, props);

    private static Candle candleAtClose(double close) {
        Candle c = new Candle();
        c.setTime("20260101_000000");
        c.setClose(close);
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

    private void runWithGap(PaperTrade trade, double close) {
        when(repo.findByTradeStatus(PaperTradeStatus.OPEN)).thenReturn(List.of(trade));
        when(repo.save(any(PaperTrade.class))).thenAnswer(i -> i.getArgument(0));
        evaluator.lastRunAt = LocalDateTime.now().minusHours(2); // > 60m since previous run → recovery
        evaluator.evaluateOpenTrades(candleAtClose(close));
    }

    @Test
    void recoveryGapWithinBoundsIsDiscardedStale() {
        PaperTrade t = bullish();
        runWithGap(t, 105); // within [SL 90, TP4 140]
        assertThat(t.getTradeStatus()).isEqualTo(PaperTradeStatus.DISCARDED);
        assertThat(t.getCloseReason()).isEqualTo(CloseReason.STALE_OPEN_TRADE);
    }

    @Test
    void recoveryGapBeyondBoundsIsAmbiguous() {
        PaperTrade t = bullish();
        runWithGap(t, 200); // beyond TP4 (140) → ambiguous which level was hit during the gap
        assertThat(t.getTradeStatus()).isEqualTo(PaperTradeStatus.DISCARDED);
        assertThat(t.getCloseReason()).isEqualTo(CloseReason.AMBIGUOUS_RECOVERY_STATE);
    }

    private static BigDecimal bd(double v) {
        return BigDecimal.valueOf(v);
    }
}
