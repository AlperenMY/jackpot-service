package com.jackpot.dto;

import java.math.BigDecimal;

public class JackpotEvaluationResponse {

    private final String betId;
    private final String jackpotId;
    private final boolean won;
    private final BigDecimal rewardAmount;

    public JackpotEvaluationResponse(String betId, String jackpotId, boolean won, BigDecimal rewardAmount) {
        this.betId = betId;
        this.jackpotId = jackpotId;
        this.won = won;
        this.rewardAmount = rewardAmount;
    }

    public String getBetId() {
        return betId;
    }

    public String getJackpotId() {
        return jackpotId;
    }

    public boolean isWon() {
        return won;
    }

    public BigDecimal getRewardAmount() {
        return rewardAmount;
    }
}
