package com.jackpot.repository;

import com.jackpot.domain.ProcessedBet;
import org.springframework.data.repository.CrudRepository;

public interface ProcessedBetRepository extends CrudRepository<ProcessedBet, String> {
}
