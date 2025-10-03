package com.jackpot.strategy.contribution;

import java.math.BigDecimal;

public interface ContributionStrategy {
    BigDecimal calculateContribution(BigDecimal betAmount, BigDecimal currentPool);
}
