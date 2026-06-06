package com.flyingbird.crypto.papertrading.repository;

import com.flyingbird.crypto.papertrading.entity.PaperCandle;
import com.flyingbird.crypto.scheduler.common.Timeframe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PaperCandleRepository extends JpaRepository<PaperCandle, Long> {

    Optional<PaperCandle> findByTimeframeAndCandleTime(Timeframe timeframe, LocalDateTime candleTime);

    boolean existsByTimeframeAndCandleTime(Timeframe timeframe, LocalDateTime candleTime);
}
