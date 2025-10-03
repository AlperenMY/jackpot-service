package com.jackpot.strategy.contribution;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class FixedContributionStrategyTest {

    @Test
    void calculatesContributionUsingFixedRate() {
        FixedContributionStrategy strategy = new FixedContributionStrategy(new BigDecimal("0.10"));

        BigDecimal contribution = strategy.calculateContribution(new BigDecimal("100"), BigDecimal.ZERO);

        assertThat(contribution).isEqualByComparingTo("10.00");
    }
}
