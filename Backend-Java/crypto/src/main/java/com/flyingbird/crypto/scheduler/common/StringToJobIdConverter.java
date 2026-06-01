package com.flyingbird.crypto.scheduler.common;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Binds the path value (fb_1m_job / fb_5m_job / fb_15m_job) to the {@link JobId}
 * enum for {@code @PathVariable JobId}. Auto-registered into the MVC conversion
 * service by Spring Boot.
 */
@Component
public class StringToJobIdConverter implements Converter<String, JobId> {

    @Override
    public JobId convert(String source) {
        return JobId.fromCode(source);
    }
}
