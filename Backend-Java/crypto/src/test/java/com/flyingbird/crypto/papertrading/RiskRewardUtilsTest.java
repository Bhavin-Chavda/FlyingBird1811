package com.flyingbird.crypto.papertrading;

import com.flyingbird.crypto.papertrading.enums.TradeDirection;
import com.flyingbird.crypto.papertrading.util.RiskRewardUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class RiskRewardUtilsTest {

    @Test
    void bullishRiskAndTakeProfits() {
        assertThat(RiskRewardUtils.validStopSide(TradeDirection.BULLISH, 100, 90)).isTrue();
        double risk = RiskRewardUtils.risk(TradeDirection.BULLISH, 100, 90).orElseThrow();
        assertThat(risk).isEqualTo(10.0);
        double[] tps = RiskRewardUtils.takeProfits(TradeDirection.BULLISH, 100, risk);
        assertThat(tps[0]).isCloseTo(110, within(1e-9));
        assertThat(tps[1]).isCloseTo(120, within(1e-9));
        assertThat(tps[2]).isCloseTo(130, within(1e-9));
        assertThat(tps[3]).isCloseTo(140, within(1e-9));
    }

    @Test
    void bearishRiskAndTakeProfits() {
        assertThat(RiskRewardUtils.validStopSide(TradeDirection.BEARISH, 100, 110)).isTrue();
        double risk = RiskRewardUtils.risk(TradeDirection.BEARISH, 100, 110).orElseThrow();
        assertThat(risk).isEqualTo(10.0);
        double[] tps = RiskRewardUtils.takeProfits(TradeDirection.BEARISH, 100, risk);
        assertThat(tps[0]).isCloseTo(90, within(1e-9));
        assertThat(tps[3]).isCloseTo(60, within(1e-9));
    }

    @Test
    void invalidStopsAreRejected() {
        // bullish stop above entry → invalid
        assertThat(RiskRewardUtils.risk(TradeDirection.BULLISH, 100, 105)).isEmpty();
        // bearish stop below entry → invalid
        assertThat(RiskRewardUtils.risk(TradeDirection.BEARISH, 100, 95)).isEmpty();
        // zero risk → invalid
        assertThat(RiskRewardUtils.risk(TradeDirection.BULLISH, 100, 100)).isEmpty();
    }
}
