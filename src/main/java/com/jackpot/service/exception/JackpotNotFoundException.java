package com.jackpot.service.exception;

public class JackpotNotFoundException extends RuntimeException {

    public JackpotNotFoundException(String jackpotId) {
        super("Jackpot not found: " + jackpotId);
    }
}
