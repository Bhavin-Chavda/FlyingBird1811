package com.flyingbird.crypto.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Market Data Properties
 *
 * Binds `market.*` — Delta Exchange candle source and EMA/buffer settings,
 * migrated from the Python fetch_data_*.py module-level config / .env values.
 */
@Data
@Component
@ConfigurationProperties(prefix = "market")
public class MarketDataProperties {

    /** Delta Exchange REST base URL (Python BASE_URL_PROD). */
    private String deltaBaseUrl;

    /** Instrument symbol (Python CANDLE_SYMBOL, default BTCUSD). */
    private String symbol = "BTCUSD";

    /** Rolling candle buffer length (Python BUFFER_LEN = 300). */
    private int bufferLength = 300;

    /** Deep-history seed multiplier (Python SEED_MULTIPLE = 10). */
    private int seedMultiple = 10;

    /** Delta product id used when building bracket orders (Python DEMO_BTCUSD_ID). Optional. */
    private String productId;

    /** HTTP timeout (connect + read) for Delta candle requests, in milliseconds. */
    private int fetchTimeoutMs = 15000;

    /** Number of attempts for a Delta fetch before giving up (>= 1). */
    private int retryAttempts = 3;

    /** Base backoff between retry attempts in ms (multiplied by attempt number). */
    private long retryBackoffMs = 1000;
}
