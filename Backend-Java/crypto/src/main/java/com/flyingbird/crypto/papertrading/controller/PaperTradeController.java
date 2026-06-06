package com.flyingbird.crypto.papertrading.controller;

import com.flyingbird.crypto.papertrading.dto.PaperTradeDetailsResponseDto;
import com.flyingbird.crypto.papertrading.service.PaperTradeQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Paper-trade read API (JWT-protected; thin). Returns all paper trades with their
 * related signal candle, newest first, with optional filters. Paper trading only —
 * no live orders.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/paper-trades")
public class PaperTradeController {

    private final PaperTradeQueryService paperTradeQueryService;

    @GetMapping
    @Operation(summary = "List paper trades",
            description = "All paper trades with related signal candle data (newest first). "
                    + "Optional filters: status (OPEN/CLOSED/DISCARDED), timeframe (1m/5m/15m/1h), tradeType "
                    + "(BULLISH/BEARISH), patternName, safeTrade, fromDate, toDate (yyyy-MM-dd).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paper trades retrieved",
                    content = @Content(schema = @Schema(implementation = PaperTradeDetailsResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<List<PaperTradeDetailsResponseDto>> getPaperTrades(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String timeframe,
            @RequestParam(required = false) String tradeType,
            @RequestParam(required = false) String patternName,
            @RequestParam(required = false) Boolean safeTrade,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        log.info("Paper trades requested | status={} timeframe={} type={} pattern={} safe={}",
                status, timeframe, tradeType, patternName, safeTrade);
        List<PaperTradeDetailsResponseDto> trades = paperTradeQueryService.getTrades(
                status, timeframe, tradeType, patternName, safeTrade, fromDate, toDate);
        return ResponseEntity.ok(trades);
    }
}
