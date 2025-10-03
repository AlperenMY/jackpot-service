package com.jackpot.service;

import com.jackpot.domain.BetContribution;
import com.jackpot.domain.Jackpot;
import com.jackpot.domain.JackpotReward;
import com.jackpot.domain.ProcessedBet;
import com.jackpot.event.BetMessage;
import com.jackpot.repository.BetContributionRepository;
import com.jackpot.repository.JackpotRepository;
import com.jackpot.repository.JackpotRewardRepository;
import com.jackpot.repository.ProcessedBetRepository;
import com.jackpot.service.exception.BetAlreadyProcessedException;
import com.jackpot.service.exception.ContributionNotFoundException;
import com.jackpot.service.exception.JackpotNotFoundException;
import com.jackpot.service.model.JackpotEvaluationResult;
import com.jackpot.strategy.contribution.ContributionStrategy;
import com.jackpot.strategy.contribution.ContributionStrategyFactory;
import com.jackpot.strategy.reward.RewardChanceStrategy;
import com.jackpot.strategy.reward.RewardChanceStrategyFactory;
import com.jackpot.util.RandomProvider;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class JackpotEvaluationService {

    private static final Logger log = LoggerFactory.getLogger(JackpotEvaluationService.class);
    private static final long PROCESSED_BET_TTL_SECONDS = 7 * 24 * 3600;

    private final JackpotRepository jackpotRepository;
    private final JackpotRewardRepository jackpotRewardRepository;
    private final ProcessedBetRepository processedBetRepository;
    private final BetContributionRepository betContributionRepository;
    private final ContributionStrategyFactory contributionStrategyFactory;
    private final RewardChanceStrategyFactory rewardChanceStrategyFactory;
    private final JackpotPoolManager poolManager;
    private final RandomProvider randomProvider;
    private final Clock clock;

    public JackpotEvaluationService(JackpotRepository jackpotRepository,
                                    JackpotRewardRepository jackpotRewardRepository,
                                    ProcessedBetRepository processedBetRepository,
                                    BetContributionRepository betContributionRepository,
                                    ContributionStrategyFactory contributionStrategyFactory,
                                    RewardChanceStrategyFactory rewardChanceStrategyFactory,
                                    JackpotPoolManager poolManager,
                                    RandomProvider randomProvider,
                                    Clock clock) {
        this.jackpotRepository = jackpotRepository;
        this.jackpotRewardRepository = jackpotRewardRepository;
        this.processedBetRepository = processedBetRepository;
        this.betContributionRepository = betContributionRepository;
        this.contributionStrategyFactory = contributionStrategyFactory;
        this.rewardChanceStrategyFactory = rewardChanceStrategyFactory;
        this.poolManager = poolManager;
        this.randomProvider = randomProvider;
        this.clock = clock;
    }

    public void recordContribution(BetMessage betMessage) {
        if (processedBetRepository.existsById(betMessage.getBetId())) {
            log.info("Duplicate bet ignored betId={} jackpotId={}",
                betMessage.getBetId(), betMessage.getJackpotId());
            throw new BetAlreadyProcessedException(betMessage.getBetId());
        }

        Jackpot jackpot = jackpotRepository.findById(betMessage.getJackpotId())
            .orElseThrow(() -> new JackpotNotFoundException(betMessage.getJackpotId()));

        ContributionStrategy contributionStrategy =
            contributionStrategyFactory.resolve(jackpot.getContributionStrategy());

        long initialPoolCents = poolManager.amountToCents(jackpot.getInitialPool());
        long currentCents = poolManager.readPoolCents(jackpot.getId(), initialPoolCents);
        BigDecimal referencePool = poolManager.centsToAmount(currentCents);

        BigDecimal contribution;
        long contributionCents;
        boolean updated = false;
        long updatedCents = currentCents;

        do {
            contribution = contributionStrategy.calculateContribution(betMessage.getBetAmount(), referencePool);
            contributionCents = poolManager.amountToCents(contribution);
            updated = poolManager.tryAddContribution(jackpot.getId(), currentCents, contributionCents);
            if (!updated) {
                currentCents = poolManager.readPoolCents(jackpot.getId(), initialPoolCents);
                referencePool = poolManager.centsToAmount(currentCents);
            } else {
                updatedCents = currentCents + contributionCents;
            }
        } while (!updated);

        BigDecimal poolAfterContribution = poolManager.centsToAmount(updatedCents);

        log.info("Contribution applied betId={} jackpotId={} contribution={} newPool={}",
            betMessage.getBetId(), betMessage.getJackpotId(), contribution, poolAfterContribution);

        jackpot.setCurrentPool(poolAfterContribution);
        jackpotRepository.save(jackpot);

        processedBetRepository.save(new ProcessedBet(
            betMessage.getBetId(),
            betMessage.getJackpotId(),
            Instant.now(clock),
            PROCESSED_BET_TTL_SECONDS));

        BetContribution contributionRecord = new BetContribution(
            betMessage.getBetId(),
            betMessage.getJackpotId(),
            betMessage.getPlayerId(),
            betMessage.getBetAmount(),
            contribution,
            poolAfterContribution,
            Instant.now(clock));
        betContributionRepository.save(contributionRecord);
    }

    public JackpotEvaluationResult evaluate(String betId) {
        BetContribution contribution = betContributionRepository.findById(betId)
            .orElseThrow(() -> new ContributionNotFoundException(betId));

        if (contribution.isEvaluated()) {
            BigDecimal existingRewardAmount = jackpotRewardRepository.findByBetId(betId)
                .map(JackpotReward::getRewardAmount)
                .orElse(BigDecimal.ZERO);
            boolean alreadyWon = existingRewardAmount.compareTo(BigDecimal.ZERO) > 0;
            log.info("Bet already evaluated betId={} jackpotId={} won={} rewardAmount={}",
                betId, contribution.getJackpotId(), alreadyWon, existingRewardAmount);
            return new JackpotEvaluationResult(
                betId,
                contribution.getJackpotId(),
                alreadyWon,
                existingRewardAmount);
        }

        Jackpot jackpot = jackpotRepository.findById(contribution.getJackpotId())
            .orElseThrow(() -> new JackpotNotFoundException(contribution.getJackpotId()));

        long initialPoolCents = poolManager.amountToCents(jackpot.getInitialPool());
        long currentCents = poolManager.readPoolCents(jackpot.getId(), initialPoolCents);
        BigDecimal currentPool = poolManager.centsToAmount(currentCents);

        RewardChanceStrategy rewardChanceStrategy =
            rewardChanceStrategyFactory.resolve(jackpot.getRewardChanceStrategy());
        double chance = clampChance(rewardChanceStrategy.determineChance(currentPool, jackpot.getInitialPool()));
        double roll = randomProvider.nextDouble();
        boolean winner = roll < chance;

        log.info("Evaluating reward betId={} jackpotId={} chance={} roll={} winner={} contributionPoolSnapshot={}",
            betId, contribution.getJackpotId(), chance, roll, winner, contribution.getJackpotAmount());

        BigDecimal rewardAmount = BigDecimal.ZERO;
        if (winner) {
            rewardAmount = currentPool;
            JackpotReward reward = new JackpotReward(
                UUID.randomUUID().toString(),
                contribution.getBetId(),
                contribution.getJackpotId(),
                contribution.getPlayerId(),
                rewardAmount,
                Instant.now(clock));
            jackpotRewardRepository.save(reward);
            log.info("Jackpot won betId={} jackpotId={} rewardAmount={} rewardedAt={}",
                contribution.getBetId(), contribution.getJackpotId(), rewardAmount, reward.getRewardedAt());

            poolManager.resetPool(jackpot.getId(), initialPoolCents);
            jackpot.setCurrentPool(jackpot.getInitialPool());
        } else {
            jackpot.setCurrentPool(currentPool);
        }

        jackpotRepository.save(jackpot);

        contribution.setEvaluated(true);
        contribution.setEvaluatedAt(Instant.now(clock));
        betContributionRepository.save(contribution);

        return new JackpotEvaluationResult(
            contribution.getBetId(),
            contribution.getJackpotId(),
            winner,
            rewardAmount);
    }

    private double clampChance(double value) {
        if (value < 0.0d) {
            return 0.0d;
        }
        if (value > 1.0d) {
            return 1.0d;
        }
        return value;
    }
}
