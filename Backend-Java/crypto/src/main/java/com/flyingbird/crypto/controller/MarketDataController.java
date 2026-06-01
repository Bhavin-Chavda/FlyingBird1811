package com.flyingbird.crypto.controller;

import com.flyingbird.crypto.exception.JobNotFoundException;
import com.flyingbird.crypto.marketdata.model.Candle;
import com.flyingbird.crypto.scheduler.common.CrossoverStateDto;
import com.flyingbird.crypto.scheduler.fifteenMinuteCandle.FifteenMinuteCandleService;
import com.flyingbird.crypto.scheduler.fiveMinuteCandle.FiveMinuteCandleService;
import com.flyingbird.crypto.scheduler.oneMinuteCandle.OneMinuteCandleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Market Data Controller (thin read API)
 *
 * Routes {timeframe} (1m / 5m / 15m) to the owning job's service. Each job owns
 * its own service + store; this controller only dispatches and returns
 * immutable snapshots/DTOs (never internal collections). JWT-protected.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/market")
public class MarketDataController {

    private final OneMinuteCandleService oneMinuteCandleService;
    private final FiveMinuteCandleService fiveMinuteCandleService;
    private final FifteenMinuteCandleService fifteenMinuteCandleService;

    @GetMapping("/{timeframe}/crossover-state")
    @Operation(summary = "Get crossover state", description = "Latest EMA-stack crossover signal state for a timeframe (1m/5m/15m)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "State retrieved",
                    content = @Content(schema = @Schema(implementation = CrossoverStateDto.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
            @ApiResponse(responseCode = "404", description = "Unknown timeframe")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<CrossoverStateDto> crossoverState(@PathVariable String timeframe) {
        CrossoverStateDto state = switch (normalize(timeframe)) {
            case "1m" -> oneMinuteCandleService.getCrossoverState();
            case "5m" -> fiveMinuteCandleService.getCrossoverState();
            case "15m" -> fifteenMinuteCandleService.getCrossoverState();
            default -> throw unknown(timeframe);
        };
        return ResponseEntity.ok(state);
    }

    @GetMapping("/{timeframe}/last-candle")
    @Operation(summary = "Get last candle", description = "Most recent candle in the buffer for a timeframe (1m/5m/15m)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Candle retrieved",
                    content = @Content(schema = @Schema(implementation = Candle.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
            @ApiResponse(responseCode = "404", description = "Unknown timeframe or empty buffer")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Candle> lastCandle(@PathVariable String timeframe) {
        Candle last = switch (normalize(timeframe)) {
            case "1m" -> oneMinuteCandleService.getLastCandle();
            case "5m" -> fiveMinuteCandleService.getLastCandle();
            case "15m" -> fifteenMinuteCandleService.getLastCandle();
            default -> throw unknown(timeframe);
        };
        if (last == null) {
            throw new JobNotFoundException("No candles available yet for timeframe '" + timeframe + "'");
        }
        return ResponseEntity.ok(last);
    }

    @GetMapping("/{timeframe}/buffer")
    @Operation(summary = "Get candle buffer", description = "Full candle buffer snapshot for a timeframe (1m/5m/15m)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Buffer retrieved",
                    content = @Content(schema = @Schema(implementation = Candle.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
            @ApiResponse(responseCode = "404", description = "Unknown timeframe")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<List<Candle>> buffer(@PathVariable String timeframe) {
        List<Candle> snapshot = switch (normalize(timeframe)) {
            case "1m" -> oneMinuteCandleService.getBufferSnapshot();
            case "5m" -> fiveMinuteCandleService.getBufferSnapshot();
            case "15m" -> fifteenMinuteCandleService.getBufferSnapshot();
            default -> throw unknown(timeframe);
        };
        return ResponseEntity.ok(snapshot);
    }

    private String normalize(String timeframe) {
        return timeframe == null ? "" : timeframe.toLowerCase();
    }

    private JobNotFoundException unknown(String timeframe) {
        return new JobNotFoundException("Unknown timeframe '" + timeframe + "' (expected 1m, 5m or 15m)");
    }
}
