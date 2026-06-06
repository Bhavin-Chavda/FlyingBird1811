package com.flyingbird.crypto.papertrading.dto;

import com.flyingbird.crypto.papertrading.enums.ChartPatternName;
import com.flyingbird.crypto.papertrading.enums.TradeDirection;
import com.flyingbird.crypto.scheduler.common.Timeframe;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * One confirmed pattern detection with its computed trade plan. Internal DTO
 * produced by {@code PatternDetector}; never persisted directly.
 */
@Data
@Builder
public class PatternDetectionResultDto {

    private boolean detected;
    private Timeframe timeframe;
    private ChartPatternName patternName;
    private TradeDirection tradeDirection;

    private LocalDateTime signalCandleTime;

    private BigDecimal entryPrice;
    private BigDecimal stopLoss;
    private BigDecimal tp1;
    private BigDecimal tp2;
    private BigDecimal tp3;
    private BigDecimal tp4;

    private BigDecimal riskPerUnit;
    private BigDecimal breakoutLevel;
    private BigDecimal atrAtDetection;
    private BigDecimal confidenceScore;

    private String detectionReason;
    private Integer candlesAnalyzed;
    private LocalDateTime patternStartTime;
    private LocalDateTime patternEndTime;
}
