package com.flyingbird.crypto.papertrading.repository;

import com.flyingbird.crypto.papertrading.entity.PaperTrade;
import com.flyingbird.crypto.papertrading.enums.ChartPatternName;
import com.flyingbird.crypto.papertrading.enums.PaperTradeStatus;
import com.flyingbird.crypto.papertrading.enums.TradeDirection;
import com.flyingbird.crypto.scheduler.common.Timeframe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaperTradeRepository extends JpaRepository<PaperTrade, Long> {

    List<PaperTrade> findByTradeStatus(PaperTradeStatus status);

    boolean existsByTimeframeAndPatternNameAndTradeTypeAndCandleTime(
            Timeframe timeframe,
            ChartPatternName patternName,
            TradeDirection tradeType,
            LocalDateTime candleTime);

    /**
     * Active-pattern guard: is there already an OPEN trade for this
     * timeframe + pattern + direction? If so, a re-detection of the still-active
     * pattern on a later candle must NOT create another trade until the existing
     * one closes.
     */
    boolean existsByTimeframeAndPatternNameAndTradeTypeAndTradeStatus(
            Timeframe timeframe,
            ChartPatternName patternName,
            TradeDirection tradeType,
            PaperTradeStatus tradeStatus);

    /** All trades, newest first, with the related candle eagerly fetched for the API. */
    @Query("select t from PaperTrade t left join fetch t.paperCandle order by t.createdAt desc")
    List<PaperTrade> findAllWithCandleNewestFirst();
}
