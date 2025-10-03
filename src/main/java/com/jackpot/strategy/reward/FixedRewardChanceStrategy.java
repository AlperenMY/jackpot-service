package com.jackpot.strategy.reward;

import java.math.BigDecimal;

public class FixedRewardChanceStrategy implements RewardChanceStrategy {

    private final double probability;

    public FixedRewardChanceStrategy(double probability) {
        this.probability = clamp(probability);
    }

    @Override
    public double determineChance(BigDecimal currentPool, BigDecimal initialPool) {
        return probability;
    }

    private double clamp(double value) {
        if (value < 0.0) {
            return 0.0;
        }
        if (value > 1.0) {
            return 1.0;
        }
        return value;
    }
}
