package com.flyingbird.crypto.service.impl;

import com.flyingbird.crypto.config.NotificationProperties;
import com.flyingbird.crypto.marketdata.model.Candle;
import com.flyingbird.crypto.marketdata.model.OrderRequest;
import com.flyingbird.crypto.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Mail Service Implementation
 *
 * Migrated from Python mailor.py (send_object_email). Sends a plain-text email
 * summarising the triggering candle and the built bracket order.
 *
 * JavaMailSender is injected via ObjectProvider so the application still boots
 * when SMTP is not configured; sending is gated on notification.email.enabled.
 * (The Python file attached JSON files; here the same content is inlined in the
 * body to keep the migration dependency-light.)
 */
@Slf4j
@Service
public class MailServiceImpl implements MailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final NotificationProperties props;

    public MailServiceImpl(ObjectProvider<JavaMailSender> mailSenderProvider,
                           NotificationProperties props) {
        this.mailSenderProvider = mailSenderProvider;
        this.props = props;
    }

    @Override
    public void sendSignalEmail(Candle candle, OrderRequest order, String subject, String details) {
        if (!props.isEnabled()) {
            log.info("Email disabled — skipping signal email | subject={}", subject);
            return;
        }
        if (props.getTo() == null || props.getTo().isEmpty()) {
            log.warn("Email enabled but no recipients (notification.email.to) — skipping");
            return;
        }
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) {
            log.warn("Email enabled but no JavaMailSender configured (spring.mail.host) — skipping");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (props.getFrom() != null && !props.getFrom().isBlank()) {
                message.setFrom(props.getFrom());
            }
            message.setTo(props.getTo().toArray(new String[0]));
            List<String> cc = props.getCc();
            if (cc != null && !cc.isEmpty()) {
                message.setCc(cc.toArray(new String[0]));
            }
            List<String> bcc = props.getBcc();
            if (bcc != null && !bcc.isEmpty()) {
                message.setBcc(bcc.toArray(new String[0]));
            }
            message.setSubject(props.getSubjectPrefix() + subject);
            message.setText(buildBody(candle, order, details));

            sender.send(message);
            log.info("Signal email sent | subject={} | recipients={}", subject, props.getTo());
        } catch (Exception e) {
            // Never let an email failure break the scheduler job.
            log.error("Failed to send signal email | subject={} | error={}", subject, e.getMessage(), e);
        }
    }

    private String buildBody(Candle candle, OrderRequest order, String details) {
        StringBuilder sb = new StringBuilder();
        sb.append("Automatic notification\n\n");
        if (details != null && !details.isBlank()) {
            sb.append(details).append("\n\n");
        }
        sb.append("CANDLE:\n").append(candle).append("\n\n");
        sb.append("----- ORDER -----\n").append(order);
        return sb.toString();
    }
}
