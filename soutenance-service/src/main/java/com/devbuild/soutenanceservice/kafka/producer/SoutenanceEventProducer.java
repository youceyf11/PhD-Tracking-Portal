package com.devbuild.soutenanceservice.kafka.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SoutenanceEventProducer {

    private final KafkaTemplate<String, Long> kafkaTemplate;

    public void sendDemandeSubmitted(Long demandeId) {
        kafkaTemplate.send("demande-submitted", demandeId);
    }

    public void sendJuryProposed(Long demandeId) {
        kafkaTemplate.send("jury-proposed", demandeId);
    }

    public void sendSoutenanceAuthorisee(Long demandeId) {
        kafkaTemplate.send("soutenance-authorisee", demandeId);
    }

    public void sendSoutenancePlanified(Long demandeId) {
        kafkaTemplate.send("soutenance-planified", demandeId);
    }

    public void sendRapportsOk(Long demandeId) {
        kafkaTemplate.send("rapport-ok", demandeId);
    }

    public void sendDureeAlerte(Long demandeId) {
        kafkaTemplate.send("duree->5>", demandeId);
    }

    public void sendDemandeRejetee(Long demandeId) {
        kafkaTemplate.send("demande-rejetee", demandeId);
    }
}
