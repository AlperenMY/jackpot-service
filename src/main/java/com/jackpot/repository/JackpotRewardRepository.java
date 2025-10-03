package com.jackpot.repository;

import com.jackpot.domain.JackpotReward;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface JackpotRewardRepository extends CrudRepository<JackpotReward, String> {

    Optional<JackpotReward> findByBetId(String betId);
}
