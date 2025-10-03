package com.jackpot.controller;

import com.jackpot.dto.BetEvaluationRequest;
import com.jackpot.dto.JackpotEvaluationResponse;
import com.jackpot.service.JackpotEvaluationService;
import com.jackpot.service.model.JackpotEvaluationResult;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jackpots")
public class JackpotEvaluationController {

    private final JackpotEvaluationService jackpotEvaluationService;

    public JackpotEvaluationController(JackpotEvaluationService jackpotEvaluationService) {
        this.jackpotEvaluationService = jackpotEvaluationService;
    }

    @PostMapping("/evaluate")
    public ResponseEntity<JackpotEvaluationResponse> evaluate(@Valid @RequestBody BetEvaluationRequest request) {
        JackpotEvaluationResult result = jackpotEvaluationService.evaluate(request.getBetId());
        JackpotEvaluationResponse response = new JackpotEvaluationResponse(
            result.getBetId(),
            result.getJackpotId(),
            result.isWon(),
            result.getRewardAmount());
        return ResponseEntity.ok(response);
    }
}
