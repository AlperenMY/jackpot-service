package com.jackpot.repository;

import com.jackpot.domain.Jackpot;
import org.springframework.data.repository.CrudRepository;

public interface JackpotRepository extends CrudRepository<Jackpot, String> {
}
