package com.flyingbird.crypto.papertrading.service;

import com.flyingbird.crypto.marketdata.model.Candle;

/**
 * Evaluates all OPEN paper trades against the latest 1-minute candle close,
 * updating TP/SL progress and closing trades on TP4 or stop-loss.
 */
public interface TradeEvaluator {

    void evaluateOpenTrades(Candle latestOneMinuteCandle);
}
