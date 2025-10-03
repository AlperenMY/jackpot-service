package com.jackpot.strategy.reward;

import java.math.BigDecimal;

public interface RewardChanceStrategy {
    double determineChance(BigDecimal currentPool, BigDecimal initialPool);
}
