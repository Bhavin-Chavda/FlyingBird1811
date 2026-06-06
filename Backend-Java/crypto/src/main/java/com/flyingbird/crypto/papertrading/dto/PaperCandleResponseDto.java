package com.flyingbird.crypto.papertrading.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Candle data nested inside a paper-trade response. */
@Data
@Builder
public class PaperCandleResponseDto {

    private Long candleId;
    private String timeframe;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime candleTime;

    private BigDecimal openPrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal closePrice;
    private BigDecimal volume;
}
