package com.jackpot.strategy.contribution;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class VariableContributionStrategy implements ContributionStrategy {

    private final BigDecimal startRate;
    private final BigDecimal minRate;
    private final BigDecimal maxRate;
    private final BigDecimal decayStep;

    public VariableContributionStrategy(BigDecimal startRate, BigDecimal minRate,
                                        BigDecimal maxRate, BigDecimal decayStep) {
        this.startRate = startRate;
        this.minRate = minRate;
        this.maxRate = maxRate;
        this.decayStep = decayStep;
    }

    @Override
    public BigDecimal calculateContribution(BigDecimal betAmount, BigDecimal currentPool) {
        BigDecimal decrement = BigDecimal.ZERO;
        if (decayStep.compareTo(BigDecimal.ZERO) > 0) {
            decrement = currentPool.divide(decayStep, 8, RoundingMode.HALF_UP);
        }
        BigDecimal effectiveRate = startRate.subtract(decrement);
        if (effectiveRate.compareTo(minRate) < 0) {
            effectiveRate = minRate;
        }
        if (effectiveRate.compareTo(maxRate) > 0) {
            effectiveRate = maxRate;
        }
        return betAmount.multiply(effectiveRate).setScale(2, RoundingMode.HALF_UP);
    }
}
