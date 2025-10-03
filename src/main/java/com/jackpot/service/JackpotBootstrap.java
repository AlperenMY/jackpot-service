package com.jackpot.service;

import com.jackpot.config.JackpotProperties;
import com.jackpot.domain.ContributionStrategyType;
import com.jackpot.domain.Jackpot;
import com.jackpot.domain.RewardChanceStrategyType;
import com.jackpot.repository.JackpotRepository;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class JackpotBootstrap implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(JackpotBootstrap.class);

    private final JackpotRepository jackpotRepository;
    private final JackpotProperties jackpotProperties;
    private final JackpotPoolManager poolManager;

    public JackpotBootstrap(JackpotRepository jackpotRepository,
                            JackpotProperties jackpotProperties,
                            JackpotPoolManager poolManager) {
        this.jackpotRepository = jackpotRepository;
        this.jackpotProperties = jackpotProperties;
        this.poolManager = poolManager;
    }

    @Override
    public void run(String... args) {
        jackpotProperties.getBootstrap().getJackpots().forEach(def -> {
            ContributionStrategyType contributionStrategy = parseContribution(def.getContributionStrategy());
            RewardChanceStrategyType rewardStrategy = parseReward(def.getRewardChanceStrategy());
            BigDecimal initialPool = def.getInitialPool();
            Jackpot jackpot = new Jackpot(
                def.getId(),
                def.getName(),
                initialPool,
                initialPool,
                contributionStrategy,
                rewardStrategy);
            jackpotRepository.save(jackpot);
            long initialCents = poolManager.amountToCents(initialPool);
            poolManager.resetPool(jackpot.getId(), initialCents);
            log.info("Bootstrap jackpot loaded jackpotId={} name={} initialPool={}",
                jackpot.getId(), jackpot.getName(), initialPool);
        });
    }

    private ContributionStrategyType parseContribution(String value) {
        return ContributionStrategyType.valueOf(value.toUpperCase());
    }

    private RewardChanceStrategyType parseReward(String value) {
        return RewardChanceStrategyType.valueOf(value.toUpperCase());
    }
}
