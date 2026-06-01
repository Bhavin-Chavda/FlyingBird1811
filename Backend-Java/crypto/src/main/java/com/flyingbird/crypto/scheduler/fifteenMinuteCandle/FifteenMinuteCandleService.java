package com.flyingbird.crypto.scheduler.fifteenMinuteCandle;

import com.flyingbird.crypto.marketdata.model.Candle;
import com.flyingbird.crypto.scheduler.common.CrossoverStateDto;

import java.util.List;

/**
 * 15-minute candle service (owned by the 15m job only).
 */
public interface FifteenMinuteCandleService {

    void seed();

    int run();

    int bufferSize();

    List<Candle> getBufferSnapshot();

    Candle getLastCandle();

    CrossoverStateDto getCrossoverState();
}
