package com.jackpot.strategy.reward;

import com.jackpot.config.JackpotProperties;
import com.jackpot.domain.RewardChanceStrategyType;
import org.springframework.stereotype.Component;

@Component
public class RewardChanceStrategyFactory {

    private final RewardChanceStrategy fixedStrategy;
    private final RewardChanceStrategy variableStrategy;

    public RewardChanceStrategyFactory(JackpotProperties properties) {
        var config = properties.getRewardChance();
        this.fixedStrategy = new FixedRewardChanceStrategy(config.getFixed().getProbability());
        this.variableStrategy = new VariableRewardChanceStrategy(
            config.getVariable().getBaseProbability(),
            config.getVariable().getGrowthFactor(),
            config.getVariable().getHardLimit(),
            config.getVariable().getMaxProbability());
    }

    public RewardChanceStrategy resolve(RewardChanceStrategyType strategyType) {
        return switch (strategyType) {
            case FIXED -> fixedStrategy;
            case VARIABLE -> variableStrategy;
        };
    }
}
