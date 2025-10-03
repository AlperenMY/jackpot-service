package com.jackpot.strategy.reward;

import java.math.BigDecimal;

public class VariableRewardChanceStrategy implements RewardChanceStrategy {

    private final double baseProbability;
    private final double growthFactor;
    private final double hardLimit;
    private final double maxProbability;

    public VariableRewardChanceStrategy(double baseProbability, double growthFactor,
                                        double hardLimit, double maxProbability) {
        this.baseProbability = baseProbability;
        this.growthFactor = growthFactor;
        this.hardLimit = hardLimit;
        this.maxProbability = maxProbability;
    }

    @Override
    public double determineChance(BigDecimal currentPool, BigDecimal initialPool) {
        double poolAmount = currentPool.doubleValue();
        if (poolAmount >= hardLimit && hardLimit > 0.0) {
            return 1.0;
        }
        double computed = baseProbability + poolAmount * growthFactor;
        if (computed < 0.0) {
            computed = 0.0;
        }
        double capped = Math.min(computed, maxProbability);
        if (capped > 1.0) {
            capped = 1.0;
        }
        return capped;
    }
}
