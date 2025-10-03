package com.jackpot.strategy.contribution;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class FixedContributionStrategy implements ContributionStrategy {

    private final BigDecimal rate;

    public FixedContributionStrategy(BigDecimal rate) {
        this.rate = rate;
    }

    @Override
    public BigDecimal calculateContribution(BigDecimal betAmount, BigDecimal currentPool) {
        return betAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
}
