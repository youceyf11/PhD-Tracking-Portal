package com.devbuild.inscriptionservice.kafka.producer;

import com.devbuild.inscriptionservice.kafka.event.DossierStatusChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
@Slf4j
public class DossierEventProducer {

    private static final String TOPIC = "dossier-status-changed";

    private final KafkaTemplate<String, DossierStatusChangedEvent> kafkaTemplate;

    public DossierEventProducer(KafkaTemplate<String, DossierStatusChangedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendDossierEvent(DossierStatusChangedEvent event) {
        log.info("Sending dossier event to Kafka: dossierId={}, eventType={}",
                event.getDossierId(), event.getEventType());

        try {
            // 🛑 FORCE SYNCHRONOUS SEND 🛑
            // .get() forces the code to wait until Kafka says "Received!"
            // If Kafka is down, this throws an Exception immediately.
            kafkaTemplate.send(TOPIC, String.valueOf(event.getDossierId()), event).get();

            log.info("✅ Successfully sent dossier event: dossierId={}", event.getDossierId());

        } catch (InterruptedException | ExecutionException e) {
            log.error("❌ Error sending message to Kafka", e);
            // Throwing RuntimeException triggers @Transactional rollback in ValidationService
            throw new RuntimeException("Failed to send Kafka event", e);
        }
    }
}