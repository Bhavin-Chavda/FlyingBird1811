package com.flyingbird.crypto.service;

import com.flyingbird.crypto.marketdata.model.Candle;

/**
 * Mail Service
 *
 * Sends crossover signal notification emails (migrated from Python mailor.py).
 */
public interface MailService {

    /**
     * Send a signal email with the triggering candle and a signal payload object
     * (e.g. the DTC trade plan / {@code PatternDetectionResultDto}). The payload is
     * generic ({@link Object}) so any DTO can be included; it may be {@code null}.
     * No-op (logged) when email is disabled or SMTP is not configured.
     */
    void sendSignalEmail(Candle candle, Object object, String subject, String details);
}
