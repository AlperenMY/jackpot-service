package com.jackpot.dto;

import jakarta.validation.constraints.NotBlank;

public class BetEvaluationRequest {

    @NotBlank
    private String betId;

    public String getBetId() {
        return betId;
    }

    public void setBetId(String betId) {
        this.betId = betId;
    }
}
