package com.flyingbird.crypto.controller;

import com.flyingbird.crypto.scheduler.common.JobDetailsResponseDto;
import com.flyingbird.crypto.scheduler.common.JobId;
import com.flyingbird.crypto.scheduler.common.Timeframe;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies the Job Details endpoint's timeframe → JobId mapping and the shape of
 * the aggregate response. Calls the controller bean directly (MockMvc/jsonpath
 * are not on this project's test classpath).
 */
@SpringBootTest
class JobDetailsControllerTest {

    @Autowired
    private JobDetailsController jobDetailsController;

    @Test
    void oneMinuteTimeframeMapsToOneMinuteJob() {
        JobDetailsResponseDto body = jobDetailsController.getJobDetails(Timeframe.ONE_MINUTE).getBody();

        assertThat(body).isNotNull();
        assertThat(body.getJobId()).isEqualTo("fb_1m_job");
        assertThat(body.getTimeframe()).isEqualTo("1m");
        assertThat(body.getCandles()).isNotNull();
        // Default scheduler.job-details.candle-count = 5.
        assertThat(body.getCandles().size()).isLessThanOrEqualTo(5);
    }

    @Test
    void allTimeframesMapToTheCorrectJobId() {
        assertThat(jobDetailsController.getJobDetails(Timeframe.ONE_MINUTE).getBody().getJobId())
                .isEqualTo(JobId.FB_1M.getCode());
        assertThat(jobDetailsController.getJobDetails(Timeframe.FIVE_MINUTE).getBody().getJobId())
                .isEqualTo(JobId.FB_5M.getCode());
        assertThat(jobDetailsController.getJobDetails(Timeframe.FIFTEEN_MINUTE).getBody().getJobId())
                .isEqualTo(JobId.FB_15M.getCode());
        assertThat(jobDetailsController.getJobDetails(Timeframe.ONE_HOUR).getBody().getJobId())
                .isEqualTo(JobId.FB_1H.getCode());
    }

    @Test
    void timeframeFromCodeRejectsAJobId() {
        // The URL path variable is a Timeframe; a job id like fb_1m_job must be rejected.
        assertThatThrownBy(() -> Timeframe.fromCode("fb_1m_job"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
