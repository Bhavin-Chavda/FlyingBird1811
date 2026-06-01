package com.flyingbird.crypto.marketdata.model;

import lombok.Data;

/**
 * Candle (in-memory market-data model)
 *
 * Migrated from the Python `Candle` dataclass (dtos/models.py).
 * OHLCV + the TradingView-style EMA stack + candle colour.
 *
 * `time` is an IST string "yyyyMMdd_HHmmss" (lexicographically sortable),
 * matching the Python format so buffer ordering/dedup behaves identically.
 *
 * EMA values may be null for the earliest candles in a series (before enough
 * history exists to seed that period) — exactly like the Python None values.
 */
@Data
public class Candle {

    private String time;
    private double open;
    private double high;
    private double low;
    private double close;
    private double volume;

    private Double ema21;
    private Double ema30;
    private Double ema35;
    private Double ema40;
    private Double ema45;
    private Double ema50;
    private Double ema60;
    private Double ema200;

    private String candleType; // "green" | "red"

    /** Get the EMA value for a given period (null if not yet computed). */
    public Double getEma(int period) {
        return switch (period) {
            case 21 -> ema21;
            case 30 -> ema30;
            case 35 -> ema35;
            case 40 -> ema40;
            case 45 -> ema45;
            case 50 -> ema50;
            case 60 -> ema60;
            case 200 -> ema200;
            default -> throw new IllegalArgumentException("Unsupported EMA period: " + period);
        };
    }

    /** Set the EMA value for a given period. */
    public void setEma(int period, Double value) {
        switch (period) {
            case 21 -> ema21 = value;
            case 30 -> ema30 = value;
            case 35 -> ema35 = value;
            case 40 -> ema40 = value;
            case 45 -> ema45 = value;
            case 50 -> ema50 = value;
            case 60 -> ema60 = value;
            case 200 -> ema200 = value;
            default -> throw new IllegalArgumentException("Unsupported EMA period: " + period);
        }
    }
}
