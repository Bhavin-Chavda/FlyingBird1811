package com.flyingbird.crypto.papertrading;

import com.flyingbird.crypto.marketdata.model.Candle;
import com.flyingbird.crypto.papertrading.dto.CrossoverStateSnapshotDto;
import com.flyingbird.crypto.papertrading.dto.PatternDetectionResultDto;
import com.flyingbird.crypto.papertrading.entity.PaperCandle;
import com.flyingbird.crypto.papertrading.entity.PaperTrade;
import com.flyingbird.crypto.papertrading.enums.ChartPatternName;
import com.flyingbird.crypto.papertrading.enums.PaperTradeStatus;
import com.flyingbird.crypto.papertrading.enums.TradeDirection;
import com.flyingbird.crypto.papertrading.repository.PaperCandleRepository;
import com.flyingbird.crypto.papertrading.repository.PaperTradeRepository;
import com.flyingbird.crypto.papertrading.service.CrossoverStateSnapshotService;
import com.flyingbird.crypto.papertrading.service.impl.PaperTradeCreationServiceImpl;
import com.flyingbird.crypto.scheduler.common.Timeframe;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies the active-OPEN-trade duplicate guard in paper-trade creation.
 */
class PaperTradeCreationServiceTest {

    private final PaperCandleRepository candleRepo = mock(PaperCandleRepository.class);
    private final PaperTradeRepository tradeRepo = mock(PaperTradeRepository.class);
    private final CrossoverStateSnapshotService crossoverSnapshotService =
            mock(CrossoverStateSnapshotService.class);
    private final PaperTradeCreationServiceImpl service =
            new PaperTradeCreationServiceImpl(candleRepo, tradeRepo, crossoverSnapshotService);

    {
        // capture() is only invoked when a trade is actually created; return a NEUTRAL snapshot.
        when(crossoverSnapshotService.capture()).thenReturn(CrossoverStateSnapshotDto.builder()
                .oneMinute("NEUTRAL").fiveMinute("NEUTRAL").fifteenMinute("NEUTRAL").oneHour("NEUTRAL").build());
    }

    private static Candle signalCandle() {
        Candle c = new Candle();
        c.setTime("20260101_120000");
        c.setOpen(100); c.setHigh(101); c.setLow(99); c.setClose(100); c.setVolume(10);
        return c;
    }

    private static PatternDetectionResultDto result(ChartPatternName p, TradeDirection d) {
        return PatternDetectionResultDto.builder()
                .detected(true).timeframe(Timeframe.FIVE_MINUTE).patternName(p).tradeDirection(d)
                .signalCandleTime(LocalDateTime.now())
                .entryPrice(bd(100)).stopLoss(bd(110))
                .tp1(bd(90)).tp2(bd(80)).tp3(bd(70)).tp4(bd(60))
                .riskPerUnit(bd(10)).confidenceScore(bd(0.7)).detectionReason("x")
                .build();
    }

    private void openExists(boolean exists) {
        when(tradeRepo.existsByTimeframeAndPatternNameAndTradeTypeAndTradeStatus(
                any(), any(), any(), eq(PaperTradeStatus.OPEN))).thenReturn(exists);
    }

    @Test
    void openDuplicateSkipsTradeAndCandle() {
        openExists(true); // an OPEN trade already exists for this tf+pattern+type

        service.createFromDetections(Timeframe.FIVE_MINUTE, signalCandle(),
                List.of(result(ChartPatternName.DOUBLE_TOP, TradeDirection.BEARISH)));

        verify(tradeRepo, never()).save(any());
        verify(candleRepo, never()).save(any()); // Edge Case G: no candle when all skipped
    }

    @Test
    void noOpenDuplicateCreatesTrade() {
        openExists(false);
        when(tradeRepo.existsByTimeframeAndPatternNameAndTradeTypeAndCandleTime(any(), any(), any(), any()))
                .thenReturn(false);
        when(candleRepo.findByTimeframeAndCandleTime(any(), any())).thenReturn(Optional.empty());
        when(candleRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        service.createFromDetections(Timeframe.FIVE_MINUTE, signalCandle(),
                List.of(result(ChartPatternName.DOUBLE_TOP, TradeDirection.BEARISH)));

        verify(candleRepo, times(1)).save(any(PaperCandle.class));
        verify(tradeRepo, times(1)).save(any(PaperTrade.class));
    }

    @Test
    void closedDuplicateAllowsNewTrade() {
        // No OPEN trade (the previous one is CLOSED) → creation proceeds.
        openExists(false);
        when(tradeRepo.existsByTimeframeAndPatternNameAndTradeTypeAndCandleTime(any(), any(), any(), any()))
                .thenReturn(false);
        when(candleRepo.findByTimeframeAndCandleTime(any(), any())).thenReturn(Optional.empty());
        when(candleRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        service.createFromDetections(Timeframe.FIVE_MINUTE, signalCandle(),
                List.of(result(ChartPatternName.DOUBLE_TOP, TradeDirection.BEARISH)));

        verify(tradeRepo, times(1)).save(any(PaperTrade.class));
    }

    @Test
    void sameCandleDuplicateSkipped() {
        openExists(false);
        when(tradeRepo.existsByTimeframeAndPatternNameAndTradeTypeAndCandleTime(any(), any(), any(), any()))
                .thenReturn(true); // identical trade already exists for this exact candle

        service.createFromDetections(Timeframe.FIVE_MINUTE, signalCandle(),
                List.of(result(ChartPatternName.DOUBLE_TOP, TradeDirection.BEARISH)));

        verify(tradeRepo, never()).save(any());
        verify(candleRepo, never()).save(any());
    }

    @Test
    void oppositeDirectionIsAllowedWhenOtherDirectionOpen() {
        // OPEN exists only for BEARISH; the BULLISH result must still be created.
        when(tradeRepo.existsByTimeframeAndPatternNameAndTradeTypeAndTradeStatus(
                any(), any(), eq(TradeDirection.BEARISH), eq(PaperTradeStatus.OPEN))).thenReturn(true);
        when(tradeRepo.existsByTimeframeAndPatternNameAndTradeTypeAndTradeStatus(
                any(), any(), eq(TradeDirection.BULLISH), eq(PaperTradeStatus.OPEN))).thenReturn(false);
        when(tradeRepo.existsByTimeframeAndPatternNameAndTradeTypeAndCandleTime(any(), any(), any(), any()))
                .thenReturn(false);
        when(candleRepo.findByTimeframeAndCandleTime(any(), any())).thenReturn(Optional.empty());
        when(candleRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        service.createFromDetections(Timeframe.FIVE_MINUTE, signalCandle(), List.of(
                result(ChartPatternName.DOUBLE_TOP, TradeDirection.BEARISH),   // skipped (open exists)
                result(ChartPatternName.DOUBLE_BOTTOM, TradeDirection.BULLISH) // created
        ));

        verify(tradeRepo, times(1)).save(any(PaperTrade.class));
    }

    private static BigDecimal bd(double v) {
        return BigDecimal.valueOf(v);
    }
}
