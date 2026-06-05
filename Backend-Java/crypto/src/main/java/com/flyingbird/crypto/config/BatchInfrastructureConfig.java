package com.flyingbird.crypto.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.EnableJdbcJobRepository;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Batch infrastructure configuration.
 *
 * <p><b>Why this class exists:</b> Spring Boot 4 / Spring Batch 6 changed the default
 * {@code JobRepository} to the in-memory {@code ResourcelessJobRepository}
 * (Boot's {@code BatchAutoConfiguration} is documented as "Spring Batch using an
 * in-memory store"). With that default, every scheduled job runs and completes in
 * memory and <b>nothing is written to the {@code BATCH_*} metadata tables</b> — which
 * is exactly why those tables existed (created by {@code spring.sql.init}) but stayed
 * empty.</p>
 *
 * <p>Adding {@link EnableJdbcJobRepository} alongside {@link EnableBatchProcessing}
 * opts into the JDBC-backed {@code JobRepository}/{@code JobOperator} so every
 * execution launched by the per-job schedulers (via {@code JobOperator.start(...)})
 * is persisted to the default Spring Batch tables (BATCH_JOB_INSTANCE,
 * BATCH_JOB_EXECUTION, BATCH_JOB_EXECUTION_PARAMS, BATCH_STEP_EXECUTION,
 * BATCH_JOB_EXECUTION_CONTEXT, BATCH_STEP_EXECUTION_CONTEXT) in MySQL {@code fly_db}.</p>
 *
 * <p>The defaults of {@link EnableJdbcJobRepository} resolve to the beans Spring Boot
 * already provides: {@code dataSourceRef="dataSource"},
 * {@code transactionManagerRef="transactionManager"}, {@code jdbcOperationsRef="jdbcTemplate"}.
 * The metadata tables are created elsewhere ({@code spring.sql.init} →
 * {@code schema-mysql.sql}); this class does not create or modify any schema.</p>
 */
@Configuration
@EnableBatchProcessing
@EnableJdbcJobRepository
public class BatchInfrastructureConfig {
}
