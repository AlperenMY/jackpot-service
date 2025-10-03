package com.jackpot.event;

import java.math.BigDecimal;

public class BetMessage {

    private String betId;
    private String jackpotId;
    private String playerId;
    private BigDecimal betAmount;

    public BetMessage() {
    }

    public BetMessage(String betId, String jackpotId, String playerId, BigDecimal betAmount) {
        this.betId = betId;
        this.jackpotId = jackpotId;
        this.playerId = playerId;
        this.betAmount = betAmount;
    }

    public String getBetId() {
        return betId;
    }

    public void setBetId(String betId) {
        this.betId = betId;
    }

    public String getJackpotId() {
        return jackpotId;
    }

    public void setJackpotId(String jackpotId) {
        this.jackpotId = jackpotId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public BigDecimal getBetAmount() {
        return betAmount;
    }

    public void setBetAmount(BigDecimal betAmount) {
        this.betAmount = betAmount;
    }
}
