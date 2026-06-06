package com.flyingbird.crypto.papertrading.service.impl;

import com.flyingbird.crypto.papertrading.dto.PaperCandleResponseDto;
import com.flyingbird.crypto.papertrading.dto.PaperTradeDetailsResponseDto;
import com.flyingbird.crypto.papertrading.entity.PaperCandle;
import com.flyingbird.crypto.papertrading.entity.PaperTrade;
import com.flyingbird.crypto.papertrading.repository.PaperTradeRepository;
import com.flyingbird.crypto.papertrading.service.PaperTradeQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Maps {@link PaperTrade} entities to API DTOs with optional in-memory filtering.
 * Read-only transaction so the lazily-related candle stays loadable; the repository
 * query also join-fetches the candle.
 */
@Service
@RequiredArgsConstructor
public class PaperTradeQueryServiceImpl implements PaperTradeQueryService {

    private final PaperTradeRepository paperTradeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PaperTradeDetailsResponseDto> getTrades(String status, String timeframe, String tradeType,
                                                        String patternName, Boolean safeTrade,
                                                        LocalDate fromDate, LocalDate toDate) {
        return paperTradeRepository.findAllWithCandleNewestFirst().stream()
                .filter(t -> match(status, t.getTradeStatus().name()))
                .filter(t -> match(timeframe, t.getTimeframe().name()) || match(timeframe, t.getTimeframe().getCode()))
                .filter(t -> match(tradeType, t.getTradeType().name()))
                .filter(t -> match(patternName, t.getPatternName().name()))
                .filter(t -> safeTrade == null || t.isSafeTrade() == safeTrade)
                .filter(t -> afterOrEqual(t.getCandleTime(), fromDate))
                .filter(t -> beforeOrEqual(t.getCandleTime(), toDate))
                .map(this::toDto)
                .toList();
    }

    private boolean match(String filter, String value) {
        return filter == null || filter.isBlank() || filter.equalsIgnoreCase(value);
    }

    private boolean afterOrEqual(LocalDateTime t, LocalDate from) {
        return from == null || (t != null && !t.toLocalDate().isBefore(from));
    }

    private boolean beforeOrEqual(LocalDateTime t, LocalDate to) {
        return to == null || (t != null && !t.toLocalDate().isAfter(to));
    }

    private PaperTradeDetailsResponseDto toDto(PaperTrade t) {
        return PaperTradeDetailsResponseDto.builder()
                .tradeId(t.getTradeId())
                .timeframe(t.getTimeframe().getCode())
                .tradeType(t.getTradeType().name())
                .tradeStatus(t.getTradeStatus().name())
                .patternName(t.getPatternName().name())
                .candleTime(t.getCandleTime())
                .tradePrice(t.getTradePrice())
                .stopLoss(t.getStopLoss())
                .initialStopLoss(t.getInitialStopLoss())
                .tp1(t.getTp1())
                .tp2(t.getTp2())
                .tp3(t.getTp3())
                .tp4(t.getTp4())
                .safeTrade(t.isSafeTrade())
                .tp1Achieved(t.isTp1Achieved())
                .tp2Achieved(t.isTp2Achieved())
                .tp3Achieved(t.isTp3Achieved())
                .tp4Achieved(t.isTp4Achieved())
                .closePrice(t.getClosePrice())
                .closeReason(t.getCloseReason() == null ? null : t.getCloseReason().name())
                .openedAt(t.getOpenedAt())
                .closedAt(t.getClosedAt())
                .lastEvaluatedAt(t.getLastEvaluatedAt())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .detectionReason(t.getDetectionReason())
                .confidenceScore(t.getConfidenceScore())
                .breakoutLevel(t.getBreakoutLevel())
                .riskAmount(t.getRiskAmount())
                .atrAtDetection(t.getAtrAtDetection())
                .patternStartTime(t.getPatternStartTime())
                .patternEndTime(t.getPatternEndTime())
                .oneMinuteCrossoverState(t.getOneMinuteCrossoverState())
                .fiveMinuteCrossoverState(t.getFiveMinuteCrossoverState())
                .fifteenMinuteCrossoverState(t.getFifteenMinuteCrossoverState())
                .oneHourCrossoverState(t.getOneHourCrossoverState())
                .paperCandle(toCandleDto(t.getPaperCandle()))
                .build();
    }

    private PaperCandleResponseDto toCandleDto(PaperCandle c) {
        if (c == null) {
            return null;
        }
        return PaperCandleResponseDto.builder()
                .candleId(c.getCandleId())
                .timeframe(c.getTimeframe().getCode())
                .candleTime(c.getCandleTime())
                .openPrice(c.getOpenPrice())
                .highPrice(c.getHighPrice())
                .lowPrice(c.getLowPrice())
                .closePrice(c.getClosePrice())
                .volume(c.getVolume())
                .build();
    }
}
