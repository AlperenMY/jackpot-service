package com.jackpot.service.exception;

public class BetAlreadyProcessedException extends RuntimeException {

    public BetAlreadyProcessedException(String betId) {
        super("Bet already processed: " + betId);
    }
}
