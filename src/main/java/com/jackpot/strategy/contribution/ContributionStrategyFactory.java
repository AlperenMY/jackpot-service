package com.jackpot.strategy.contribution;

import com.jackpot.config.JackpotProperties;
import com.jackpot.domain.ContributionStrategyType;
import org.springframework.stereotype.Component;

@Component
public class ContributionStrategyFactory {

    private final ContributionStrategy fixedStrategy;
    private final ContributionStrategy variableStrategy;

    public ContributionStrategyFactory(JackpotProperties properties) {
        var config = properties.getContribution();
        this.fixedStrategy = new FixedContributionStrategy(config.getFixed().getRate());
        this.variableStrategy = new VariableContributionStrategy(
            config.getVariable().getStartRate(),
            config.getVariable().getMinRate(),
            config.getVariable().getMaxRate(),
            config.getVariable().getDecayStep());
    }

    public ContributionStrategy resolve(ContributionStrategyType strategyType) {
        return switch (strategyType) {
            case FIXED -> fixedStrategy;
            case VARIABLE -> variableStrategy;
        };
    }
}
