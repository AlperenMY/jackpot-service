package com.jackpot.config;

import com.jackpot.event.BetMessage;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public ProducerFactory<String, BetMessage> betProducerFactory(KafkaProperties kafkaProperties) {
        var props = kafkaProperties.buildProducerProperties(null);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, BetMessage> betKafkaTemplate(ProducerFactory<String, BetMessage> betProducerFactory) {
        return new KafkaTemplate<>(betProducerFactory);
    }

    @Bean
    public ConsumerFactory<String, BetMessage> betConsumerFactory(KafkaProperties kafkaProperties) {
        var props = kafkaProperties.buildConsumerProperties(null);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BetMessage> betKafkaListenerContainerFactory(
        ConsumerFactory<String, BetMessage> betConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, BetMessage> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(betConsumerFactory);
        factory.getContainerProperties().setMissingTopicsFatal(false);
        return factory;
    }

    @Bean
    public NewTopic betTopic(JackpotProperties jackpotProperties) {
        return TopicBuilder.name(jackpotProperties.getKafka().getTopic())
            .partitions(3)
            .replicas(1)
            .build();
    }
}
