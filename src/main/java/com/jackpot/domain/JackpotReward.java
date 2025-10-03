package com.jackpot.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("jackpot_reward")
public class JackpotReward implements Serializable {

    @Id
    private String id;
    private String betId;
    private String jackpotId;
    private String playerId;
    private BigDecimal rewardAmount;
    private Instant rewardedAt;

    public JackpotReward() {
    }

    public JackpotReward(String id, String betId, String jackpotId, String playerId,
                         BigDecimal rewardAmount, Instant rewardedAt) {
        this.id = id;
        this.betId = betId;
        this.jackpotId = jackpotId;
        this.playerId = playerId;
        this.rewardAmount = rewardAmount;
        this.rewardedAt = rewardedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public BigDecimal getRewardAmount() {
        return rewardAmount;
    }

    public void setRewardAmount(BigDecimal rewardAmount) {
        this.rewardAmount = rewardAmount;
    }

    public Instant getRewardedAt() {
        return rewardedAt;
    }

    public void setRewardedAt(Instant rewardedAt) {
        this.rewardedAt = rewardedAt;
    }
}
