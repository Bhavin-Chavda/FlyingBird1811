package com.flyingbird.crypto.scheduler.oneMinuteCandle;

import com.flyingbird.crypto.marketdata.model.Candle;
import com.flyingbird.crypto.scheduler.common.CrossoverStateDto;

import java.util.List;

/**
 * 1-minute candle service (owned by the 1m job only).
 */
public interface OneMinuteCandleService {

    void seed();

    int run();

    int bufferSize();

    List<Candle> getBufferSnapshot();

    Candle getLastCandle();

    CrossoverStateDto getCrossoverState();
}
