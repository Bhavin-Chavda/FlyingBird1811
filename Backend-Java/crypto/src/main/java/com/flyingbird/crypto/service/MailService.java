package com.flyingbird.crypto.service;

import com.flyingbird.crypto.marketdata.model.Candle;
import com.flyingbird.crypto.marketdata.model.OrderRequest;

/**
 * Mail Service
 *
 * Sends crossover signal notification emails (migrated from Python mailor.py).
 */
public interface MailService {

    /**
     * Send a signal email with the triggering candle and the built order.
     * No-op (logged) when email is disabled or SMTP is not configured.
     */
    void sendSignalEmail(Candle candle, OrderRequest order, String subject, String details);
}
