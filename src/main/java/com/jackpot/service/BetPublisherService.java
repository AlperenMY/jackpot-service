package com.jackpot.service;

import com.jackpot.config.JackpotProperties;
import com.jackpot.dto.BetRequest;
import com.jackpot.event.BetMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class BetPublisherService {

    private static final Logger log = LoggerFactory.getLogger(BetPublisherService.class);

    private final KafkaTemplate<String, BetMessage> kafkaTemplate;
    private final JackpotProperties jackpotProperties;

    public BetPublisherService(KafkaTemplate<String, BetMessage> kafkaTemplate,
                               JackpotProperties jackpotProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.jackpotProperties = jackpotProperties;
    }

    public void publish(BetRequest request) {
        BetMessage message = new BetMessage(
            request.getBetId(),
            request.getJackpotId(),
            request.getPlayerId(),
            request.getBetAmount());
        String topic = jackpotProperties.getKafka().getTopic();
        log.info("Publishing bet betId={} jackpotId={} amount={}",
            request.getBetId(), request.getJackpotId(), request.getBetAmount());
        kafkaTemplate.send(topic, request.getJackpotId(), message)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish bet betId={} jackpotId={} error={}",
                        request.getBetId(), request.getJackpotId(), ex.getMessage(), ex);
                } else {
                    log.info("Bet published successfully betId={} jackpotId={}",
                        request.getBetId(), request.getJackpotId());
                }
            });
    }
}
