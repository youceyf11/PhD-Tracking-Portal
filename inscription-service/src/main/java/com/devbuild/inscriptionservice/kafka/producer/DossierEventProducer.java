package com.devbuild.inscriptionservice.kafka.producer;


import com.devbuild.inscriptionservice.kafka.event.DossierStatusChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

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

        CompletableFuture<SendResult<String, DossierStatusChangedEvent>> future =
                kafkaTemplate.send(TOPIC, String.valueOf(event.getDossierId()), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Successfully sent dossier event: dossierId={}, offset={}",
                        event.getDossierId(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send dossier event: dossierId={}",
                        event.getDossierId(), ex);
            }
        });
    }
}
