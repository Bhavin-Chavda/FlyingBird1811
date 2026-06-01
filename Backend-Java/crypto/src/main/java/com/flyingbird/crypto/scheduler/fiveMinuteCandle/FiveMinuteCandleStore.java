package com.flyingbird.crypto.scheduler.fiveMinuteCandle;

import com.flyingbird.crypto.config.MarketDataProperties;
import com.flyingbird.crypto.marketdata.model.Candle;
import com.flyingbird.crypto.scheduler.common.CandleCalculationUtils;
import com.flyingbird.crypto.scheduler.common.CrossoverStateDto;
import com.flyingbird.crypto.scheduler.common.SchedulerConstants;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 5-minute candle data store (owned by the 5m job only).
 *
 * Encapsulates the rolling candle buffer + crossover signal state, guarded by
 * its own {@link ReentrantReadWriteLock}. Writes (seed/append/record) take the
 * write lock; reads return immutable copies under the read lock — the internal
 * deque is never exposed. Slow work (Delta fetch) happens in the service BEFORE
 * these methods are called, so the lock is held only for in-memory mutation.
 */
@Component
public class FiveMinuteCandleStore {

    private final String jobId;
    private final int maxLen;

    private final Deque<Candle> buffer = new ArrayDeque<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    // crossover signal state (guarded by the same lock)
    private long calls;
    private String lastSignal;        // null until first evaluation
    private String lastCandleTime;
    private String lastCheckedAtIst;

    public FiveMinuteCandleStore(MarketDataProperties props) {
        this.jobId = "fb_5m_job";
        this.maxLen = props.getBufferLength();
    }

    /** Replace the whole buffer (seeding). Trims to the most recent maxLen. */
    public void seedReplace(List<Candle> candles) {
        lock.writeLock().lock();
        try {
            buffer.clear();
            int from = Math.max(0, candles.size() - maxLen);
            buffer.addAll(candles.subList(from, candles.size()));
        } finally {
            lock.writeLock().unlock();
        }
    }

    /** Append the latest closed candle with EMAs; returns true if appended. */
    public boolean appendLatest(Candle latest) {
        lock.writeLock().lock();
        try {
            return CandleCalculationUtils.appendWithEma(buffer, latest, maxLen);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Atomically record an evaluation and report whether this is a fresh
     * NEUTRAL -> BULLISH/BEARISH transition (i.e. a signal should be emailed).
     */
    public boolean recordEvaluation(String signal, String candleTime, String checkedAtIst) {
        lock.writeLock().lock();
        try {
            boolean transition = (SchedulerConstants.SIGNAL_BULLISH.equals(signal)
                    || SchedulerConstants.SIGNAL_BEARISH.equals(signal))
                    && SchedulerConstants.SIGNAL_NEUTRAL.equals(lastSignal);
            calls++;
            lastSignal = signal;
            lastCandleTime = candleTime;
            lastCheckedAtIst = checkedAtIst;
            return transition;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<Candle> snapshot() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(buffer);
        } finally {
            lock.readLock().unlock();
        }
    }

    public int size() {
        lock.readLock().lock();
        try {
            return buffer.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    public Candle lastCandle() {
        lock.readLock().lock();
        try {
            return buffer.peekLast();
        } finally {
            lock.readLock().unlock();
        }
    }

    public CrossoverStateDto crossoverSnapshot() {
        lock.readLock().lock();
        try {
            return CrossoverStateDto.builder()
                    .jobId(jobId)
                    .calls(calls)
                    .lastSignal(lastSignal)
                    .lastCandleTime(lastCandleTime)
                    .lastCheckedAtIst(lastCheckedAtIst)
                    .build();
        } finally {
            lock.readLock().unlock();
        }
    }
}
