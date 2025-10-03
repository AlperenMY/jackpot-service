package com.jackpot.dto;

public class BetPublishResponse {

    private final String status;

    public BetPublishResponse(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
