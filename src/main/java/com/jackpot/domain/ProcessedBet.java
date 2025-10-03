package com.jackpot.domain;

import java.io.Serializable;
import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@RedisHash("processed_bet")
public class ProcessedBet implements Serializable {

    @Id
    private String betId;
    private String jackpotId;
    private Instant processedAt;
    @TimeToLive
    private Long ttlSeconds;

    public ProcessedBet() {
    }

    public ProcessedBet(String betId, String jackpotId, Instant processedAt, Long ttlSeconds) {
        this.betId = betId;
        this.jackpotId = jackpotId;
        this.processedAt = processedAt;
        this.ttlSeconds = ttlSeconds;
    }

    public String getBetId() {
        return betId;
    }

    public void setBetId(String betId) {
        this.betId = betId;
    }

    public String getJackpotId() {
        return jackpotId;
    }

    public void setJackpotId(String jackpotId) {
        this.jackpotId = jackpotId;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }

    public Long getTtlSeconds() {
        return ttlSeconds;
    }

    public void setTtlSeconds(Long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }
}
