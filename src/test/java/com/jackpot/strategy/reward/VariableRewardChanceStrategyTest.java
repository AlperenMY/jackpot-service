package com.jackpot.strategy.reward;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class VariableRewardChanceStrategyTest {

    @Test
    void reachesFullProbabilityAtHardLimit() {
        VariableRewardChanceStrategy strategy = new VariableRewardChanceStrategy(0.01, 0.0001, 1000.0, 0.9);

        double chanceBeforeLimit = strategy.determineChance(new BigDecimal("500"), BigDecimal.ZERO);
        double chanceAtLimit = strategy.determineChance(new BigDecimal("1000"), BigDecimal.ZERO);
        double chanceBeyondLimit = strategy.determineChance(new BigDecimal("1500"), BigDecimal.ZERO);

        assertThat(chanceBeforeLimit).isLessThan(1.0);
        assertThat(chanceAtLimit).isEqualTo(1.0);
        assertThat(chanceBeyondLimit).isEqualTo(1.0);
    }
}
