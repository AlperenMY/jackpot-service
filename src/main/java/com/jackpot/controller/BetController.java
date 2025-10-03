package com.jackpot.controller;

import com.jackpot.dto.BetPublishResponse;
import com.jackpot.dto.BetRequest;
import com.jackpot.service.BetPublisherService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bets")
public class BetController {

    private final BetPublisherService betPublisherService;

    public BetController(BetPublisherService betPublisherService) {
        this.betPublisherService = betPublisherService;
    }

    @PostMapping
    public ResponseEntity<BetPublishResponse> publish(@Valid @RequestBody BetRequest request) {
        betPublisherService.publish(request);
        return ResponseEntity.accepted().body(new BetPublishResponse("Bet accepted for processing"));
    }
}
