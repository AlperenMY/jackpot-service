package com.jackpot.service.exception;

public class ContributionNotFoundException extends RuntimeException {

    public ContributionNotFoundException(String betId) {
        super("Contribution not found for bet: " + betId);
    }
}
