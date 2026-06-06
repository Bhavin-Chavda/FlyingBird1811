package com.flyingbird.crypto.papertrading.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * API view of a paper trade with its related signal candle. Enum values are sent
 * as strings (timeframe as its code, e.g. "1m"); the frontend types mirror this.
 */
@Data
@Builder
public class PaperTradeDetailsResponseDto {

    private Long tradeId;
    private String timeframe;
    private String tradeType;
    private String tradeStatus;
    private String patternName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime candleTime;

    private BigDecimal tradePrice;
    private BigDecimal stopLoss;
    private BigDecimal initialStopLoss;
    private BigDecimal tp1;
    private BigDecimal tp2;
    private BigDecimal tp3;
    private BigDecimal tp4;

    private boolean safeTrade;
    private boolean tp1Achieved;
    private boolean tp2Achieved;
    private boolean tp3Achieved;
    private boolean tp4Achieved;

    private BigDecimal closePrice;
    private String closeReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime openedAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime closedAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastEvaluatedAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private String detectionReason;
    private BigDecimal confidenceScore;
    private BigDecimal breakoutLevel;
    private BigDecimal riskAmount;
    private BigDecimal atrAtDetection;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime patternStartTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime patternEndTime;

    // Crossover-state snapshot captured at trade creation (BULLISH/BEARISH/NEUTRAL).
    private String oneMinuteCrossoverState;
    private String fiveMinuteCrossoverState;
    private String fifteenMinuteCrossoverState;
    private String oneHourCrossoverState;

    private PaperCandleResponseDto paperCandle;
}
