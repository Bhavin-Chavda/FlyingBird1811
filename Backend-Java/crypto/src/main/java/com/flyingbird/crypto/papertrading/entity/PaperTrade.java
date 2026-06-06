package com.flyingbird.crypto.papertrading.entity;

import com.flyingbird.crypto.papertrading.enums.ChartPatternName;
import com.flyingbird.crypto.papertrading.enums.CloseReason;
import com.flyingbird.crypto.papertrading.enums.PaperTradeStatus;
import com.flyingbird.crypto.papertrading.enums.TradeDirection;
import com.flyingbird.crypto.scheduler.common.Timeframe;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A simulated (paper) trade created from a confirmed chart pattern. Carries the
 * full trade plan (entry/SL/TP1-4) and live progress (TP/SL flags, safeTrade,
 * status). Evaluated every 1m against the latest close. Never places a live order.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "paper_trades",
        uniqueConstraints = @UniqueConstraint(name = "uk_paper_trade_dedup",
                columnNames = {"timeframe", "pattern_name", "trade_type", "candle_time"}),
        indexes = {
                @Index(name = "idx_paper_trade_status", columnList = "trade_status"),
                @Index(name = "idx_paper_trade_timeframe", columnList = "timeframe"),
                @Index(name = "idx_paper_trade_pattern", columnList = "pattern_name"),
                @Index(name = "idx_paper_trade_type", columnList = "trade_type"),
                @Index(name = "idx_paper_trade_candle_time", columnList = "candle_time"),
                @Index(name = "idx_paper_trade_created_at", columnList = "created_at")
        })
public class PaperTrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trade_id")
    private Long tradeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "timeframe", length = 20, nullable = false)
    private Timeframe timeframe;

    @Enumerated(EnumType.STRING)
    @Column(name = "trade_type", length = 16, nullable = false)
    private TradeDirection tradeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "trade_status", length = 16, nullable = false)
    private PaperTradeStatus tradeStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "pattern_name", length = 48, nullable = false)
    private ChartPatternName patternName;

    @Column(name = "candle_time", nullable = false)
    private LocalDateTime candleTime;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candle_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private PaperCandle paperCandle;

    // Crossover-state SNAPSHOT at trade creation (BULLISH/BEARISH/NEUTRAL string,
    // matching SchedulerConstants.SIGNAL_*). Never updated by TradeEvaluator.
    @Column(name = "one_minute_crossover_state", length = 16)
    private String oneMinuteCrossoverState;

    @Column(name = "five_minute_crossover_state", length = 16)
    private String fiveMinuteCrossoverState;

    @Column(name = "fifteen_minute_crossover_state", length = 16)
    private String fifteenMinuteCrossoverState;

    @Column(name = "one_hour_crossover_state", length = 16)
    private String oneHourCrossoverState;

    @Column(name = "trade_price", precision = 20, scale = 8)
    private BigDecimal tradePrice;

    @Column(name = "stop_loss", precision = 20, scale = 8)
    private BigDecimal stopLoss;

    @Column(name = "initial_stop_loss", precision = 20, scale = 8)
    private BigDecimal initialStopLoss;

    @Column(name = "tp1", precision = 20, scale = 8)
    private BigDecimal tp1;

    @Column(name = "tp2", precision = 20, scale = 8)
    private BigDecimal tp2;

    @Column(name = "tp3", precision = 20, scale = 8)
    private BigDecimal tp3;

    @Column(name = "tp4", precision = 20, scale = 8)
    private BigDecimal tp4;

    @Column(name = "safe_trade", nullable = false)
    private boolean safeTrade;

    @Column(name = "tp1_achieved", nullable = false)
    private boolean tp1Achieved;

    @Column(name = "tp2_achieved", nullable = false)
    private boolean tp2Achieved;

    @Column(name = "tp3_achieved", nullable = false)
    private boolean tp3Achieved;

    @Column(name = "tp4_achieved", nullable = false)
    private boolean tp4Achieved;

    @Column(name = "close_price", precision = 20, scale = 8)
    private BigDecimal closePrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "close_reason", length = 40)
    private CloseReason closeReason;

    @Column(name = "opened_at")
    private LocalDateTime openedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "last_evaluated_at")
    private LocalDateTime lastEvaluatedAt;

    @Column(name = "confidence_score", precision = 6, scale = 4)
    private BigDecimal confidenceScore;

    @Column(name = "detection_reason", length = 500)
    private String detectionReason;

    @Column(name = "pattern_start_time")
    private LocalDateTime patternStartTime;

    @Column(name = "pattern_end_time")
    private LocalDateTime patternEndTime;

    @Column(name = "breakout_level", precision = 20, scale = 8)
    private BigDecimal breakoutLevel;

    @Column(name = "risk_amount", precision = 20, scale = 8)
    private BigDecimal riskAmount;

    @Column(name = "atr_at_detection", precision = 20, scale = 8)
    private BigDecimal atrAtDetection;

    @Column(name = "created_by_version", length = 32)
    private String createdByVersion;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;
}
