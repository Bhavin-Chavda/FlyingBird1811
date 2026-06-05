package com.flyingbird.crypto.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for the per-timeframe signal-email enable flags. Pure logic — no
 * Spring context needed.
 */
class NotificationPropertiesTest {

    @Test
    void allTimeframesEnabledByDefault() {
        NotificationProperties props = new NotificationProperties();
        assertThat(props.isEmailEnabledFor("1m")).isTrue();
        assertThat(props.isEmailEnabledFor("5m")).isTrue();
        assertThat(props.isEmailEnabledFor("15m")).isTrue();
        assertThat(props.isEmailEnabledFor("1h")).isTrue();
    }

    @Test
    void disablingOneTimeframeDoesNotAffectOthers() {
        NotificationProperties props = new NotificationProperties();
        props.getSignal().setFiveMinute(false);

        assertThat(props.isEmailEnabledFor("5m")).isFalse();   // disabled
        assertThat(props.isEmailEnabledFor("1m")).isTrue();    // others unchanged
        assertThat(props.isEmailEnabledFor("15m")).isTrue();
        assertThat(props.isEmailEnabledFor("1h")).isTrue();
    }

    @Test
    void eachTimeframeFlagMapsToItsResolution() {
        NotificationProperties props = new NotificationProperties();
        props.getSignal().setOneMinute(false);
        props.getSignal().setFifteenMinute(false);
        props.getSignal().setOneHour(false);

        assertThat(props.isEmailEnabledFor("1m")).isFalse();
        assertThat(props.isEmailEnabledFor("15m")).isFalse();
        assertThat(props.isEmailEnabledFor("1h")).isFalse();
        assertThat(props.isEmailEnabledFor("5m")).isTrue();
    }

    @Test
    void unknownOrNullResolutionFailsOpen() {
        NotificationProperties props = new NotificationProperties();
        assertThat(props.isEmailEnabledFor("3m")).isTrue();
        assertThat(props.isEmailEnabledFor(null)).isTrue();
    }
}
