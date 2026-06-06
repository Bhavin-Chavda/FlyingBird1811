package com.flyingbird.crypto.papertrading.service;

import com.flyingbird.crypto.marketdata.model.Candle;
import com.flyingbird.crypto.papertrading.dto.PatternDetectionResultDto;
import com.flyingbird.crypto.scheduler.common.Timeframe;

import java.util.List;

/**
 * Persists confirmed pattern detections as OPEN paper trades (find-or-create the
 * signal {@code PaperCandle}, dedup-guarded {@code PaperTrade} insert).
 */
public interface PaperTradeCreationService {

    void createFromDetections(Timeframe timeframe, Candle signalCandle, List<PatternDetectionResultDto> results);
}
