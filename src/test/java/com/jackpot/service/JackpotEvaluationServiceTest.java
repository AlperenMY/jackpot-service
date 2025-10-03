package com.jackpot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jackpot.config.JackpotProperties;
import com.jackpot.domain.BetContribution;
import com.jackpot.domain.ContributionStrategyType;
import com.jackpot.domain.Jackpot;
import com.jackpot.domain.JackpotReward;
import com.jackpot.domain.RewardChanceStrategyType;
import com.jackpot.event.BetMessage;
import com.jackpot.repository.BetContributionRepository;
import com.jackpot.repository.JackpotRepository;
import com.jackpot.repository.JackpotRewardRepository;
import com.jackpot.repository.ProcessedBetRepository;
import com.jackpot.service.exception.BetAlreadyProcessedException;
import com.jackpot.service.model.JackpotEvaluationResult;
import com.jackpot.strategy.contribution.ContributionStrategyFactory;
import com.jackpot.strategy.reward.RewardChanceStrategyFactory;
import com.jackpot.util.RandomProvider;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class JackpotEvaluationServiceTest {

    private JackpotRepository jackpotRepository;
    private JackpotRewardRepository jackpotRewardRepository;
    private ProcessedBetRepository processedBetRepository;
    private BetContributionRepository betContributionRepository;
    private ContributionStrategyFactory contributionStrategyFactory;
    private RewardChanceStrategyFactory rewardChanceStrategyFactory;
    private JackpotPoolManager poolManager;
    private RandomProvider randomProvider;
    private Clock clock;

    @BeforeEach
    void setUp() {
        jackpotRepository = mock(JackpotRepository.class);
        jackpotRewardRepository = mock(JackpotRewardRepository.class);
        processedBetRepository = mock(ProcessedBetRepository.class);
        betContributionRepository = mock(BetContributionRepository.class);
        poolManager = mock(JackpotPoolManager.class);
        randomProvider = mock(RandomProvider.class);
        clock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC);

        JackpotProperties properties = new JackpotProperties();
        properties.getContribution().getFixed().setRate(new BigDecimal("0.10"));
        properties.getContribution().getVariable().setStartRate(new BigDecimal("0.15"));
        properties.getContribution().getVariable().setMinRate(new BigDecimal("0.05"));
        properties.getContribution().getVariable().setMaxRate(new BigDecimal("0.25"));
        properties.getContribution().getVariable().setDecayStep(new BigDecimal("5000"));
        properties.getRewardChance().getFixed().setProbability(0.5);
        properties.getRewardChance().getVariable().setBaseProbability(0.1);
        properties.getRewardChance().getVariable().setGrowthFactor(0.0001);
        properties.getRewardChance().getVariable().setHardLimit(10000.0);
        properties.getRewardChance().getVariable().setMaxProbability(0.9);

        contributionStrategyFactory = new ContributionStrategyFactory(properties);
        rewardChanceStrategyFactory = new RewardChanceStrategyFactory(properties);
    }

    @Test
    void recordsContributionAndEvaluatesWin() {
        Jackpot jackpot = new Jackpot(
            "daily",
            "Daily Jackpot",
            new BigDecimal("1000.00"),
            new BigDecimal("1000.00"),
            ContributionStrategyType.FIXED,
            RewardChanceStrategyType.FIXED);

        when(jackpotRepository.findById("daily")).thenReturn(Optional.of(jackpot));
        when(jackpotRepository.save(any(Jackpot.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(processedBetRepository.existsById("bet-1")).thenReturn(false);
        when(jackpotRewardRepository.findByBetId("bet-1")).thenReturn(Optional.empty());

        long initialCents = 100000L;
        AtomicLong storedPool = new AtomicLong(initialCents);
        AtomicReference<BetContribution> storedContribution = new AtomicReference<>();

        when(poolManager.amountToCents(any(BigDecimal.class))).thenAnswer(invocation -> {
            BigDecimal amount = invocation.getArgument(0);
            return amount.setScale(2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();
        });
        when(poolManager.centsToAmount(anyLong())).thenAnswer(invocation -> {
            long cents = invocation.getArgument(0);
            return BigDecimal.valueOf(cents).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        });
        when(poolManager.readPoolCents("daily", initialCents)).thenAnswer(invocation -> storedPool.get());
        when(poolManager.tryAddContribution(eq("daily"), eq(initialCents), anyLong())).thenAnswer(invocation -> {
            long contributionCents = invocation.getArgument(2);
            storedPool.addAndGet(contributionCents);
            return true;
        });
        when(betContributionRepository.save(any(BetContribution.class))).thenAnswer(invocation -> {
            BetContribution contribution = invocation.getArgument(0);
            storedContribution.set(contribution);
            return contribution;
        });
        when(betContributionRepository.findById("bet-1")).thenAnswer(invocation -> Optional.ofNullable(storedContribution.get()));

        BetMessage betMessage = new BetMessage("bet-1", "daily", "player-1", new BigDecimal("50.00"));

        JackpotEvaluationService service = new JackpotEvaluationService(
            jackpotRepository,
            jackpotRewardRepository,
            processedBetRepository,
            betContributionRepository,
            contributionStrategyFactory,
            rewardChanceStrategyFactory,
            poolManager,
            randomProvider,
            clock);

        service.recordContribution(betMessage);

        assertThat(storedContribution.get()).isNotNull();
        assertThat(storedContribution.get().isEvaluated()).isFalse();

        when(randomProvider.nextDouble()).thenReturn(0.01);

        JackpotEvaluationResult result = service.evaluate("bet-1");

        assertThat(result.isWon()).isTrue();
        assertThat(result.getRewardAmount()).isEqualByComparingTo("1005.00");

        ArgumentCaptor<JackpotReward> rewardCaptor = ArgumentCaptor.forClass(JackpotReward.class);
        verify(jackpotRewardRepository).save(rewardCaptor.capture());
        assertThat(rewardCaptor.getValue().getRewardAmount()).isEqualByComparingTo("1005.00");

        verify(poolManager).resetPool("daily", initialCents);
        verify(processedBetRepository).save(any());
        verify(betContributionRepository, times(2)).save(any(BetContribution.class));
        assertThat(storedContribution.get().isEvaluated()).isTrue();
    }

    @Test
    void rejectsDuplicateBets() {
        when(processedBetRepository.existsById("bet-dup")).thenReturn(true);

        JackpotEvaluationService service = new JackpotEvaluationService(
            jackpotRepository,
            jackpotRewardRepository,
            processedBetRepository,
            betContributionRepository,
            contributionStrategyFactory,
            rewardChanceStrategyFactory,
            poolManager,
            randomProvider,
            clock);

        BetMessage betMessage = new BetMessage("bet-dup", "daily", "player-1", new BigDecimal("10.00"));

        org.junit.jupiter.api.Assertions.assertThrows(BetAlreadyProcessedException.class,
            () -> service.recordContribution(betMessage));
    }

    @Test
    void returnsExistingRewardWhenContributionAlreadyEvaluated() {
        BetContribution contribution = new BetContribution(
            "bet-2",
            "daily",
            "player-1",
            new BigDecimal("10.00"),
            new BigDecimal("1.00"),
            new BigDecimal("1001.00"),
            Instant.now(clock));
        contribution.setEvaluated(true);

        when(betContributionRepository.findById("bet-2")).thenReturn(Optional.of(contribution));
        when(jackpotRewardRepository.findByBetId("bet-2")).thenReturn(Optional.of(
            new JackpotReward(
                "reward-1",
                "bet-2",
                "daily",
                "player-1",
                new BigDecimal("1001.00"),
                Instant.now(clock))));

        JackpotEvaluationService service = new JackpotEvaluationService(
            jackpotRepository,
            jackpotRewardRepository,
            processedBetRepository,
            betContributionRepository,
            contributionStrategyFactory,
            rewardChanceStrategyFactory,
            poolManager,
            randomProvider,
            clock);

        BetMessage betMessage = new BetMessage("bet-2", "daily", "player-1", new BigDecimal("10.00"));

        JackpotEvaluationResult result = service.evaluate("bet-2");

        assertThat(result.isWon()).isTrue();
        assertThat(result.getRewardAmount()).isEqualByComparingTo("1001.00");
        verify(jackpotRewardRepository, times(0)).save(any(JackpotReward.class));
    }
}
