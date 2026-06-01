package com.flyingbird.crypto.scheduler.fiveMinuteCandle;

import com.flyingbird.crypto.marketdata.model.Candle;
import com.flyingbird.crypto.scheduler.common.CrossoverStateDto;

import java.util.List;

/**
 * 5-minute candle service (owned by the 5m job only).
 */
public interface FiveMinuteCandleService {

    /** Initial seed: fetch deep history, fill EMAs, replace the buffer (last 300). */
    void seed();

    /** One update cycle (append latest / refill on fetch error). Returns buffer size. */
    int run();

    int bufferSize();

    List<Candle> getBufferSnapshot();

    Candle getLastCandle();

    CrossoverStateDto getCrossoverState();
}
