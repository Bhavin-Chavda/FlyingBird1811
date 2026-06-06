package com.flyingbird.crypto.papertrading.entity;

import com.flyingbird.crypto.scheduler.common.Timeframe;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * The signal candle that produced one or more paper trades. One PaperCandle per
 * (timeframe, candleTime) — reused if the same closed candle triggers multiple
 * patterns. OHLCV only; SL/TP belong to {@link PaperTrade}.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "paper_candles",
        uniqueConstraints = @UniqueConstraint(name = "uk_paper_candle_tf_time",
                columnNames = {"timeframe", "candle_time"}),
        indexes = @Index(name = "idx_paper_candle_time", columnList = "candle_time"))
public class PaperCandle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "candle_id")
    private Long candleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "timeframe", length = 20, nullable = false)
    private Timeframe timeframe;

    @Column(name = "candle_time", nullable = false)
    private LocalDateTime candleTime;

    @Column(name = "open_price", precision = 20, scale = 8)
    private BigDecimal openPrice;

    @Column(name = "high_price", precision = 20, scale = 8)
    private BigDecimal highPrice;

    @Column(name = "low_price", precision = 20, scale = 8)
    private BigDecimal lowPrice;

    @Column(name = "close_price", precision = 20, scale = 8)
    private BigDecimal closePrice;

    @Column(name = "volume", precision = 30, scale = 8)
    private BigDecimal volume;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
