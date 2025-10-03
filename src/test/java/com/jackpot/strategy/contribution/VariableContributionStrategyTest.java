package com.jackpot.strategy.contribution;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class VariableContributionStrategyTest {

    @Test
    void clampsBetweenConfiguredBoundsAndRespondsToPoolSize() {
        VariableContributionStrategy strategy = new VariableContributionStrategy(
            new BigDecimal("0.20"),
            new BigDecimal("0.05"),
            new BigDecimal("0.25"),
            new BigDecimal("1000"));

        BigDecimal minimalPoolContribution = strategy.calculateContribution(
            new BigDecimal("100"), new BigDecimal("50"));
        BigDecimal largePoolContribution = strategy.calculateContribution(
            new BigDecimal("100"), new BigDecimal("5000"));

        assertThat(minimalPoolContribution).isGreaterThan(largePoolContribution);
        assertThat(largePoolContribution).isEqualByComparingTo("5.00");
        assertThat(minimalPoolContribution).isLessThanOrEqualTo(new BigDecimal("25.00"));
    }
}
