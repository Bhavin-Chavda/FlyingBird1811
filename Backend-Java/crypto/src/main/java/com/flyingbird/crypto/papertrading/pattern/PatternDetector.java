package com.flyingbird.crypto.papertrading.pattern;

import com.flyingbird.crypto.marketdata.model.Candle;
import com.flyingbird.crypto.papertrading.dto.PatternDetectionResultDto;
import com.flyingbird.crypto.scheduler.common.Timeframe;

import java.util.List;

/**
 * Pure (no-DB) chart-pattern detector. Given an oldest-first list of closed
 * candles, returns the confirmed patterns whose breakout/breakdown is validated
 * by the latest candle close. Thread-safe and side-effect-free.
 */
public interface PatternDetector {

    List<PatternDetectionResultDto> detectPatterns(List<Candle> candles, Timeframe timeframe);
}
