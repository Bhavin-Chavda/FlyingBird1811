package com.flyingbird.crypto.scheduler.common;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Binds the market-data path value (1m / 5m / 15m) to the {@link Timeframe} enum
 * for {@code @PathVariable Timeframe}. Auto-registered into the MVC conversion
 * service. Any other value (e.g. an {@code fb_*} job id) throws
 * IllegalArgumentException → handled as 400 by the GlobalExceptionHandler.
 */
@Component
public class StringToTimeframeConverter implements Converter<String, Timeframe> {

    @Override
    public Timeframe convert(String source) {
        return Timeframe.fromCode(source);
    }
}
