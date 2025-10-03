package com.jackpot.config;

import com.jackpot.util.RandomProvider;
import java.util.Random;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RandomConfig {

    @Bean
    public RandomProvider randomProvider(JackpotProperties jackpotProperties) {
        return new SeededRandomProvider(jackpotProperties.getRandom().getSeed());
    }

    static class SeededRandomProvider implements RandomProvider {
        private final Random random;

        SeededRandomProvider(long seed) {
            this.random = new Random(seed);
        }

        @Override
        public double nextDouble() {
            return random.nextDouble();
        }
    }
}
