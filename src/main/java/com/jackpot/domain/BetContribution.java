package com.jackpot.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("bet_contribution")
public class BetContribution implements Serializable {

    @Id
    private String betId;
    private String jackpotId;
    private String playerId;
    private BigDecimal stakeAmount;
    private BigDecimal contributionAmount;
    private BigDecimal jackpotAmount;
    private Instant createdAt;
    private boolean evaluated;
    private Instant evaluatedAt;

    public BetContribution() {
    }

    public BetContribution(String betId,
                            String jackpotId,
                            String playerId,
                            BigDecimal stakeAmount,
                            BigDecimal contributionAmount,
                            BigDecimal jackpotAmount,
                            Instant createdAt) {
        this.betId = betId;
        this.jackpotId = jackpotId;
        this.playerId = playerId;
        this.stakeAmount = stakeAmount;
        this.contributionAmount = contributionAmount;
        this.jackpotAmount = jackpotAmount;
        this.createdAt = createdAt;
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

    public BigDecimal getStakeAmount() {
        return stakeAmount;
    }

    public void setStakeAmount(BigDecimal stakeAmount) {
        this.stakeAmount = stakeAmount;
    }

    public BigDecimal getContributionAmount() {
        return contributionAmount;
    }

    public void setContributionAmount(BigDecimal contributionAmount) {
        this.contributionAmount = contributionAmount;
    }

    public BigDecimal getJackpotAmount() {
        return jackpotAmount;
    }

    public void setJackpotAmount(BigDecimal jackpotAmount) {
        this.jackpotAmount = jackpotAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isEvaluated() {
        return evaluated;
    }

    public void setEvaluated(boolean evaluated) {
        this.evaluated = evaluated;
    }

    public Instant getEvaluatedAt() {
        return evaluatedAt;
    }

    public void setEvaluatedAt(Instant evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
    }
}
