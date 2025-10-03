package com.jackpot.config;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jackpot")
public class JackpotProperties {

    private Kafka kafka = new Kafka();
    private Random random = new Random();
    private Contribution contribution = new Contribution();
    private RewardChance rewardChance = new RewardChance();
    private Bootstrap bootstrap = new Bootstrap();

    public Kafka getKafka() {
        return kafka;
    }

    public void setKafka(Kafka kafka) {
        this.kafka = kafka;
    }

    public Random getRandom() {
        return random;
    }

    public void setRandom(Random random) {
        this.random = random;
    }

    public Contribution getContribution() {
        return contribution;
    }

    public void setContribution(Contribution contribution) {
        this.contribution = contribution;
    }

    public RewardChance getRewardChance() {
        return rewardChance;
    }

    public void setRewardChance(RewardChance rewardChance) {
        this.rewardChance = rewardChance;
    }

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public void setBootstrap(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    public static class Kafka {
        private String topic;

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }
    }

    public static class Random {
        private long seed;

        public long getSeed() {
            return seed;
        }

        public void setSeed(long seed) {
            this.seed = seed;
        }
    }

    public static class Contribution {
        private Fixed fixed = new Fixed();
        private Variable variable = new Variable();
        private double clampTolerance = 1e-6;

        public Fixed getFixed() {
            return fixed;
        }

        public void setFixed(Fixed fixed) {
            this.fixed = fixed;
        }

        public Variable getVariable() {
            return variable;
        }

        public void setVariable(Variable variable) {
            this.variable = variable;
        }

        public double getClampTolerance() {
            return clampTolerance;
        }

        public void setClampTolerance(double clampTolerance) {
            this.clampTolerance = clampTolerance;
        }

        public static class Fixed {
            private BigDecimal rate = BigDecimal.ZERO;

            public BigDecimal getRate() {
                return rate;
            }

            public void setRate(BigDecimal rate) {
                this.rate = rate;
            }
        }

        public static class Variable {
            private BigDecimal startRate = BigDecimal.ZERO;
            private BigDecimal minRate = BigDecimal.ZERO;
            private BigDecimal maxRate = BigDecimal.ONE;
            private BigDecimal decayStep = BigDecimal.ONE;

            public BigDecimal getStartRate() {
                return startRate;
            }

            public void setStartRate(BigDecimal startRate) {
                this.startRate = startRate;
            }

            public BigDecimal getMinRate() {
                return minRate;
            }

            public void setMinRate(BigDecimal minRate) {
                this.minRate = minRate;
            }

            public BigDecimal getMaxRate() {
                return maxRate;
            }

            public void setMaxRate(BigDecimal maxRate) {
                this.maxRate = maxRate;
            }

            public BigDecimal getDecayStep() {
                return decayStep;
            }

            public void setDecayStep(BigDecimal decayStep) {
                this.decayStep = decayStep;
            }
        }
    }

    public static class RewardChance {
        private Fixed fixed = new Fixed();
        private Variable variable = new Variable();

        public Fixed getFixed() {
            return fixed;
        }

        public void setFixed(Fixed fixed) {
            this.fixed = fixed;
        }

        public Variable getVariable() {
            return variable;
        }

        public void setVariable(Variable variable) {
            this.variable = variable;
        }

        public static class Fixed {
            private double probability;

            public double getProbability() {
                return probability;
            }

            public void setProbability(double probability) {
                this.probability = probability;
            }
        }

        public static class Variable {
            private double baseProbability;
            private double growthFactor;
            private double hardLimit;
            private double maxProbability = 1.0;

            public double getBaseProbability() {
                return baseProbability;
            }

            public void setBaseProbability(double baseProbability) {
                this.baseProbability = baseProbability;
            }

            public double getGrowthFactor() {
                return growthFactor;
            }

            public void setGrowthFactor(double growthFactor) {
                this.growthFactor = growthFactor;
            }

            public double getHardLimit() {
                return hardLimit;
            }

            public void setHardLimit(double hardLimit) {
                this.hardLimit = hardLimit;
            }

            public double getMaxProbability() {
                return maxProbability;
            }

            public void setMaxProbability(double maxProbability) {
                this.maxProbability = maxProbability;
            }
        }
    }

    public static class Bootstrap {
        private List<JackpotDefinition> jackpots = List.of();

        public List<JackpotDefinition> getJackpots() {
            return jackpots;
        }

        public void setJackpots(List<JackpotDefinition> jackpots) {
            this.jackpots = jackpots;
        }

        public static class JackpotDefinition {
            private String id;
            private String name;
            private BigDecimal initialPool;
            private String contributionStrategy;
            private String rewardChanceStrategy;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public BigDecimal getInitialPool() {
                return initialPool;
            }

            public void setInitialPool(BigDecimal initialPool) {
                this.initialPool = initialPool;
            }

            public String getContributionStrategy() {
                return contributionStrategy;
            }

            public void setContributionStrategy(String contributionStrategy) {
                this.contributionStrategy = contributionStrategy;
            }

            public String getRewardChanceStrategy() {
                return rewardChanceStrategy;
            }

            public void setRewardChanceStrategy(String rewardChanceStrategy) {
                this.rewardChanceStrategy = rewardChanceStrategy;
            }
        }
    }
}
