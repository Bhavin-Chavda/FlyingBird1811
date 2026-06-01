package com.flyingbird.crypto.marketdata.client;

import com.flyingbird.crypto.config.MarketDataProperties;
import com.flyingbird.crypto.marketdata.model.Candle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

/**
 * Delta Candle Client
 *
 * Migrated from the Python fetch_data_*.py HTTP layer:
 * - GET {base}/v2/history/candles?resolution&symbol&start&end
 * - filters to CLOSED candles (time < current bucket start), sorted ascending
 * - maps raw rows to Candle (EMAs left null; filled later by CandleCalculationUtils)
 *
 * Public, unauthenticated Delta endpoint — no API key required.
 */
@Slf4j
@Component
public class DeltaCandleClient {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter TS_FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").withZone(IST);

    private final MarketDataProperties props;
    private final RestClient restClient;

    public DeltaCandleClient(MarketDataProperties props) {
        this.props = props;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(props.getFetchTimeoutMs()));
        factory.setReadTimeout(Duration.ofMillis(props.getFetchTimeoutMs()));
        this.restClient = RestClient.builder()
                .baseUrl(props.getDeltaBaseUrl())
                .requestFactory(factory)
                .build();
    }

    /** Latest CLOSED candle for a timeframe (generic infra — each job passes its own resolution/bucket). */
    public Candle getLatestCandle(String resolution, int bucketSeconds) {
        long end = floorToBucket(Instant.now().getEpochSecond(), bucketSeconds);
        long start = end - (long) bucketSeconds * 3;
        List<DeltaRow> closed = fetchClosed(resolution, start, end);
        if (closed.isEmpty()) {
            throw new IllegalStateException("No closed " + resolution + " candles returned from Delta API");
        }
        return toCandle(closed.get(closed.size() - 1));
    }

    /** Last N CLOSED candles for a timeframe. */
    public List<Candle> fetchLastNCandles(String resolution, int bucketSeconds, int n) {
        long end = floorToBucket(Instant.now().getEpochSecond(), bucketSeconds);
        long start = end - (long) bucketSeconds * (n + 5);
        List<DeltaRow> closed = fetchClosed(resolution, start, end);
        if (closed.isEmpty()) {
            throw new IllegalStateException("No closed " + resolution + " candles returned from Delta API");
        }
        int from = Math.max(0, closed.size() - n);
        return closed.subList(from, closed.size()).stream().map(this::toCandle).toList();
    }

    /**
     * Fetch + filter closed candles, retrying up to {@code market.retry-attempts}
     * times when Delta is unreachable or returns an improper response.
     */
    private List<DeltaRow> fetchClosed(String resolution, long start, long end) {
        int maxAttempts = Math.max(1, props.getRetryAttempts());
        RuntimeException lastError = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                DeltaResponse resp = restClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/v2/history/candles")
                                .queryParam("resolution", resolution)
                                .queryParam("symbol", props.getSymbol())
                                .queryParam("start", start)
                                .queryParam("end", end)
                                .build())
                        .retrieve()
                        .body(DeltaResponse.class);

                if (resp == null) {
                    throw new IllegalStateException("Empty response from Delta API");
                }
                if (resp.success() != null && !resp.success()) {
                    throw new IllegalStateException("Delta API returned success=false");
                }
                List<DeltaRow> rows = resp.result() == null ? List.of() : resp.result();
                return rows.stream()
                        .filter(r -> r.time() < end)              // only closed candles
                        .sorted(Comparator.comparingLong(DeltaRow::time))
                        .toList();
            } catch (RuntimeException e) {
                lastError = e;
                log.warn("[{}] Delta fetch attempt {}/{} failed (resolution={}): {}",
                        Thread.currentThread().getName(), attempt, maxAttempts, resolution, e.getMessage());
                if (attempt < maxAttempts) {
                    sleepBackoff(attempt);
                }
            }
        }
        throw new IllegalStateException(
                "Delta API fetch failed after " + maxAttempts + " attempts (resolution=" + resolution + ")",
                lastError);
    }

    private void sleepBackoff(int attempt) {
        try {
            Thread.sleep(props.getRetryBackoffMs() * attempt);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private Candle toCandle(DeltaRow r) {
        Candle c = new Candle();
        c.setTime(TS_FMT.format(Instant.ofEpochSecond(r.time())));
        c.setOpen(r.open());
        c.setHigh(r.high());
        c.setLow(r.low());
        c.setClose(r.close());
        c.setVolume(r.volume());
        c.setCandleType(r.close() > r.open() ? "green" : "red");
        return c;
    }

    private static long floorToBucket(long ts, int seconds) {
        return ts - (ts % seconds);
    }

    /** Delta /v2/history/candles response envelope. */
    public record DeltaResponse(Boolean success, List<DeltaRow> result) {
    }

    /** A single OHLCV row from Delta. */
    public record DeltaRow(long time, double open, double high, double low, double close, double volume) {
    }
}
