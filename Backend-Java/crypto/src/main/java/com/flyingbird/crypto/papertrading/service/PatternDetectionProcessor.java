package com.flyingbird.crypto.papertrading.service;

import com.flyingbird.crypto.marketdata.model.Candle;
import com.flyingbird.crypto.papertrading.dto.PatternDetectionResultDto;
import com.flyingbird.crypto.papertrading.pattern.PatternDetector;
import com.flyingbird.crypto.scheduler.common.Timeframe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Runs the (pure) {@link PatternDetector} on a candle snapshot and persists any
 * confirmed patterns as paper trades. Shared by the async listener and the
 * synchronous fallback. Never throws — a detection failure must not affect the
 * scheduler.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PatternDetectionProcessor {

    private final PatternDetector patternDetector;
    private final PaperTradeCreationService paperTradeCreationService;

    public void process(Timeframe timeframe, List<Candle> candles) {
        if (candles == null || candles.isEmpty()) {
            return;
        }
        try {
            List<PatternDetectionResultDto> results = patternDetector.detectPatterns(candles, timeframe);
            if (results.isEmpty()) {
                log.debug("[paper-trading] no patterns detected | {} candles={}", timeframe.getCode(), candles.size());
                return;
            }
            log.info("[paper-trading] {} pattern(s) detected | {}", results.size(), timeframe.getCode());
            Candle latest = candles.get(candles.size() - 1);
            paperTradeCreationService.createFromDetections(timeframe, latest, results);
        } catch (Exception e) {
            log.error("[paper-trading] detection failed | {}: {}", timeframe.getCode(), e.getMessage(), e);
        }
    }
}
