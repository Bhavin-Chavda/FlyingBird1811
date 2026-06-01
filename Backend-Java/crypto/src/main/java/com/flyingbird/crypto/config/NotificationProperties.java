package com.flyingbird.crypto.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Notification (email) Properties
 *
 * Binds `notification.email.*` — crossover signal email recipients/config,
 * migrated from the Python mailor.py env vars. Disabled by default so the app
 * runs without SMTP configured.
 */
@Data
@Component
@ConfigurationProperties(prefix = "notification.email")
public class NotificationProperties {

    private boolean enabled = false;
    private String from;
    private List<String> to = new ArrayList<>();
    private List<String> cc = new ArrayList<>();
    private List<String> bcc = new ArrayList<>();
    private String subjectPrefix = "";
}
