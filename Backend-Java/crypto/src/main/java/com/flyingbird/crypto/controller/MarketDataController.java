package com.flyingbird.crypto.controller;

import com.flyingbird.crypto.exception.JobNotFoundException;
import com.flyingbird.crypto.marketdata.model.Candle;
import com.flyingbird.crypto.scheduler.common.CrossoverStateDto;
import com.flyingbird.crypto.scheduler.common.Timeframe;
import com.flyingbird.crypto.scheduler.fifteenMinuteCandle.FifteenMinuteCandleService;
import com.flyingbird.crypto.scheduler.fiveMinuteCandle.FiveMinuteCandleService;
import com.flyingbird.crypto.scheduler.hourlyCandle.HourlyCandleService;
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
 * The URL accepts ONLY the {@link Timeframe} values 1m / 5m / 15m (an {@code fb_*}
 * job id is rejected as 400). {@code Timeframe} maps 1:1 to the scheduler
 * {@link com.flyingbird.crypto.scheduler.common.JobId} (via {@code toJobId()}),
 * so market and scheduler data stay correlated with no separate mapping table.
 * Each job owns its own service + store; this controller only dispatches and
 * returns immutable snapshots/DTOs. JWT-protected.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/market")
public class MarketDataController {

    private final OneMinuteCandleService oneMinuteCandleService;
    private final FiveMinuteCandleService fiveMinuteCandleService;
    private final FifteenMinuteCandleService fifteenMinuteCandleService;
    private final HourlyCandleService hourlyCandleService;

    @GetMapping("/{timeframe}/crossover-state")
    @Operation(summary = "Get crossover state",
            description = "Latest EMA-stack crossover signal state for a timeframe (1m / 5m / 15m / 1h)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "State retrieved",
                    content = @Content(schema = @Schema(implementation = CrossoverStateDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid timeframe"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<CrossoverStateDto> crossoverState(@PathVariable Timeframe timeframe) {
        CrossoverStateDto state = switch (timeframe) {
            case ONE_MINUTE -> oneMinuteCandleService.getCrossoverState();
            case FIVE_MINUTE -> fiveMinuteCandleService.getCrossoverState();
            case FIFTEEN_MINUTE -> fifteenMinuteCandleService.getCrossoverState();
            case ONE_HOUR -> hourlyCandleService.getCrossoverState();
        };
        return ResponseEntity.ok(state);
    }

    @GetMapping("/{timeframe}/last-candle")
    @Operation(summary = "Get last candle",
            description = "Most recent candle in the buffer for a timeframe (1m / 5m / 15m / 1h)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Candle retrieved",
                    content = @Content(schema = @Schema(implementation = Candle.class))),
            @ApiResponse(responseCode = "400", description = "Invalid timeframe"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
            @ApiResponse(responseCode = "404", description = "Empty buffer (not seeded yet)")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Candle> lastCandle(@PathVariable Timeframe timeframe) {
        Candle last = switch (timeframe) {
            case ONE_MINUTE -> oneMinuteCandleService.getLastCandle();
            case FIVE_MINUTE -> fiveMinuteCandleService.getLastCandle();
            case FIFTEEN_MINUTE -> fifteenMinuteCandleService.getLastCandle();
            case ONE_HOUR -> hourlyCandleService.getLastCandle();
        };
        if (last == null) {
            throw new JobNotFoundException("No candles available yet for timeframe '" + timeframe.getCode() + "'");
        }
        return ResponseEntity.ok(last);
    }

    @GetMapping("/{timeframe}/buffer")
    @Operation(summary = "Get candle buffer",
            description = "Full candle buffer snapshot for a timeframe (1m / 5m / 15m / 1h)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Buffer retrieved",
                    content = @Content(schema = @Schema(implementation = Candle.class))),
            @ApiResponse(responseCode = "400", description = "Invalid timeframe"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<List<Candle>> buffer(@PathVariable Timeframe timeframe) {
        List<Candle> snapshot = switch (timeframe) {
            case ONE_MINUTE -> oneMinuteCandleService.getBufferSnapshot();
            case FIVE_MINUTE -> fiveMinuteCandleService.getBufferSnapshot();
            case FIFTEEN_MINUTE -> fifteenMinuteCandleService.getBufferSnapshot();
            case ONE_HOUR -> hourlyCandleService.getBufferSnapshot();
        };
        return ResponseEntity.ok(snapshot);
    }
}
