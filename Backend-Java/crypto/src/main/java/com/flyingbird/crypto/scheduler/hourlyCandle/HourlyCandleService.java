package com.flyingbird.crypto.scheduler.hourlyCandle;

import com.flyingbird.crypto.marketdata.model.Candle;
import com.flyingbird.crypto.scheduler.common.CrossoverStateDto;

import java.util.List;

/**
 * Hourly candle service (owned by the 1h job only).
 */
public interface HourlyCandleService {

    void seed();

    int run();

    int bufferSize();

    List<Candle> getBufferSnapshot();

    Candle getLastCandle();

    CrossoverStateDto getCrossoverState();
}
