package com.flyingbird.crypto.papertrading.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Executors for the paper-trading feature.
 *
 * <ul>
 *   <li>{@code patternDetectorExecutor} — bounded pool for async pattern detection;
 *       multiple timeframe events (e.g. 1m+5m+15m at minute 15) run concurrently and
 *       none are skipped.</li>
 *   <li>{@code paperTradeEvaluatorExecutor} — single thread so trade evaluations are
 *       strictly sequential (no overlapping/parallel updates).</li>
 * </ul>
 */
@Slf4j
@Configuration
@EnableAsync
public class PaperTradingAsyncConfig {

    @Bean("patternDetectorExecutor")
    public ThreadPoolTaskExecutor patternDetectorExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(2);
        ex.setMaxPoolSize(4);
        ex.setQueueCapacity(500);
        ex.setThreadNamePrefix("pattern-detector-");
        ex.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        ex.setWaitForTasksToCompleteOnShutdown(true);
        ex.setAwaitTerminationSeconds(20);
        ex.initialize();
        return ex;
    }

    @Bean("paperTradeEvaluatorExecutor")
    public ThreadPoolTaskExecutor paperTradeEvaluatorExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(1);
        ex.setMaxPoolSize(1);
        ex.setQueueCapacity(200);
        ex.setThreadNamePrefix("paper-evaluator-");
        ex.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        ex.setWaitForTasksToCompleteOnShutdown(true);
        ex.setAwaitTerminationSeconds(20);
        ex.initialize();
        return ex;
    }
}
