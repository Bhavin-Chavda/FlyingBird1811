package com.flyingbird.crypto.papertrading.service;

import com.flyingbird.crypto.papertrading.dto.PaperTradeDetailsResponseDto;

import java.time.LocalDate;
import java.util.List;

/**
 * Read API for paper trades (entity → DTO). Optional in-memory filters; returns
 * newest first.
 */
public interface PaperTradeQueryService {

    List<PaperTradeDetailsResponseDto> getTrades(String status,
                                                 String timeframe,
                                                 String tradeType,
                                                 String patternName,
                                                 Boolean safeTrade,
                                                 LocalDate fromDate,
                                                 LocalDate toDate);
}
