package com.jackpot.service;

import com.jackpot.event.BetMessage;
import com.jackpot.service.exception.BetAlreadyProcessedException;
import com.jackpot.service.exception.JackpotNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class BetConsumerService {

    private static final Logger log = LoggerFactory.getLogger(BetConsumerService.class);

    private final JackpotEvaluationService jackpotEvaluationService;

    public BetConsumerService(JackpotEvaluationService jackpotEvaluationService) {
        this.jackpotEvaluationService = jackpotEvaluationService;
    }

    @KafkaListener(topics = "${jackpot.kafka.topic}", containerFactory = "betKafkaListenerContainerFactory")
    public void consumeBet(BetMessage betMessage) {
        log.info("Consuming bet betId={} jackpotId={} amount={}",
            betMessage.getBetId(), betMessage.getJackpotId(), betMessage.getBetAmount());
        try {
            jackpotEvaluationService.recordContribution(betMessage);
        } catch (BetAlreadyProcessedException ex) {
            log.info("Ignoring already processed bet betId={}", betMessage.getBetId());
        } catch (JackpotNotFoundException ex) {
            log.error("Skipping bet because jackpot is missing betId={} jackpotId={}",
                betMessage.getBetId(), betMessage.getJackpotId());
        } catch (Exception ex) {
            log.error("Unexpected error while processing bet betId={} jackpotId={}",
                betMessage.getBetId(), betMessage.getJackpotId(), ex);
            throw ex;
        }
    }
}
