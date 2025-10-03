package com.jackpot.domain;

import java.io.Serializable;
import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("jackpot")
public class Jackpot implements Serializable {

    @Id
    private String id;
    private String name;
    private BigDecimal initialPool;
    private BigDecimal currentPool;
    private ContributionStrategyType contributionStrategy;
    private RewardChanceStrategyType rewardChanceStrategy;

    public Jackpot() {
    }

    public Jackpot(String id, String name, BigDecimal initialPool, BigDecimal currentPool,
                   ContributionStrategyType contributionStrategy,
                   RewardChanceStrategyType rewardChanceStrategy) {
        this.id = id;
        this.name = name;
        this.initialPool = initialPool;
        this.currentPool = currentPool;
        this.contributionStrategy = contributionStrategy;
        this.rewardChanceStrategy = rewardChanceStrategy;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getInitialPool() {
        return initialPool;
    }

    public void setInitialPool(BigDecimal initialPool) {
        this.initialPool = initialPool;
    }

    public BigDecimal getCurrentPool() {
        return currentPool;
    }

    public void setCurrentPool(BigDecimal currentPool) {
        this.currentPool = currentPool;
    }

    public ContributionStrategyType getContributionStrategy() {
        return contributionStrategy;
    }

    public void setContributionStrategy(ContributionStrategyType contributionStrategy) {
        this.contributionStrategy = contributionStrategy;
    }

    public RewardChanceStrategyType getRewardChanceStrategy() {
        return rewardChanceStrategy;
    }

    public void setRewardChanceStrategy(RewardChanceStrategyType rewardChanceStrategy) {
        this.rewardChanceStrategy = rewardChanceStrategy;
    }
}
