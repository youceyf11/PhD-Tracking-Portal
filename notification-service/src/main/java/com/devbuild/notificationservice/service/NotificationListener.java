package com.devbuild.notificationservice.service;

import com.devbuild.notificationservice.client.SoutenanceServiceClient;
import com.devbuild.notificationservice.client.UserServiceClient;
import com.devbuild.notificationservice.dto.DemandeInfo;
import com.devbuild.notificationservice.dto.DossierStatusChangedEvent;
import com.devbuild.notificationservice.dto.UserResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    private final EmailService emailService;
    private final PdfGeneratorService pdfService;
    private final UserServiceClient userServiceClient;
    private final SoutenanceServiceClient soutenanceClient;
    private final ObjectMapper objectMapper;

    /**
     * Handles Inscription Events (JSON String).
     * We manually parse the JSON to avoid "Poison Pill" loops in Kafka.
     */
    @KafkaListener(topics = "dossier-status-changed", groupId = "notification-group-FINAL")
    public void handleDossierEvent(String eventJson) {
        try {
            log.info("📥 RAW Message received: {}", eventJson);

            // 1. Manually Parse JSON to DTO
            DossierStatusChangedEvent event = objectMapper.readValue(eventJson, DossierStatusChangedEvent.class);

            log.info("✅ Parsed Event: ID={} | Statut={} | Sujet={}",
                    event.getDossierId(), event.getNouveauStatut(), event.getSujetThese());

            // 2. Fetch User Info
            UserResponse user = userServiceClient.getUserById(event.getDoctorantId());
            if (user == null || user.getEmail() == null) {
                log.warn("⚠️ User info not found for ID: {}", event.getDoctorantId());
                return;
            }

            String email = user.getEmail();
            String fullName = user.getPrenom() + " " + user.getNom();

            // 3. Process Logic
            if ("VALIDE".equals(event.getNouveauStatut())) {
                log.info("📄 Generating Attestation for {}", email);

                byte[] pdf = pdfService.generateAttestationInscription(fullName, String.valueOf(LocalDate.now().getYear()));

                String body = "<h1>Félicitations " + user.getPrenom() + "</h1>" +
                        "<p>Votre inscription a été validée par l'administration.</p>" +
                        "<p>Veuillez trouver ci-joint votre <b>Attestation d'inscription</b>.</p>";

                emailService.sendEmailWithAttachment(email, "Votre Attestation d'Inscription", body, pdf, "attestation_inscription.pdf");
            }
            else if ("REJETE".equals(event.getNouveauStatut())) {
                String body = "<h1>Bonjour " + user.getPrenom() + "</h1>" +
                        "<p>Votre dossier a été rejeté.</p>" +
                        "<p><b>Motif :</b> " + event.getCommentaire() + "</p>";

                emailService.sendEmail(email, "Mise à jour de votre dossier", body);
            }

        } catch (Exception e) {
            // 🛑 CATCH ALL EXCEPTIONS to prevent Infinite Loop in Kafka
            // This marks the message as "processed" even if it failed, so Kafka moves on.
            log.error("❌ Error processing dossier event. Skipping message.", e);
        }
    }

    /**
     * Handles Soutenance Events (Payload is usually just an ID string).
     */
    @KafkaListener(topics = {"soutenance-authorisee", "demande-submitted", "soutenance-planified"}, groupId = "notification-group-FINAL")
    public void handleSoutenanceEvent(ConsumerRecord<String, String> record) {
        String topic = record.topic();
        String payload = record.value();

        log.info("📥 Soutenance Event received: Topic={}, Payload={}", topic, payload);

        try {
            // Clean payload (remove quotes if it came as "123")
            String cleanId = payload.replace("\"", "");
            Long demandeId = Long.parseLong(cleanId);

            // 1. Fetch Demande Details
            DemandeInfo demande = soutenanceClient.getDemandeInfo(demandeId);

            // 2. Fetch User Details
            UserResponse user = userServiceClient.getUserById(demande.getDoctorantId());

            if (user == null || user.getEmail() == null) {
                log.warn("⚠️ User info not found for Doctorant ID: {}", demande.getDoctorantId());
                return;
            }

            String fullName = user.getPrenom() + " " + user.getNom();
            String email = user.getEmail();

            // 3. Handle specific topics
            switch (topic) {
                case "demande-submitted":
                    emailService.sendEmail(email, "Accusé de réception",
                            "<h1>Bonjour " + user.getPrenom() + "</h1>" +
                                    "<p>Votre demande de soutenance a bien été reçue.</p>");
                    break;

                case "soutenance-authorisee":
                    byte[] pdf = pdfService.generateAutorisationSoutenance(fullName, "À planifier", null, null);
                    emailService.sendEmailWithAttachment(email, "Soutenance Autorisée !",
                            "<h1>Félicitations !</h1><p>Votre soutenance est autorisée.</p>",
                            pdf, "autorisation_soutenance.pdf");
                    break;

                case "soutenance-planified":
                    String dateStr = (demande.getDateSoutenance() != null) ? demande.getDateSoutenance().toString() : "N/A";
                    String timeStr = (demande.getHeureSoutenance() != null) ? demande.getHeureSoutenance().toString() : "N/A";
                    byte[] pdfPlanif = pdfService.generateAutorisationSoutenance(fullName, dateStr, timeStr, demande.getLieuSoutenance());
                    emailService.sendEmailWithAttachment(email, "Convocation à la soutenance",
                            "<h1>Convocation</h1>" +
                                    "<p>Votre soutenance est prévue le <b>" + dateStr + "</b>.</p>",
                            pdfPlanif, "convocation.pdf");
                    break;
            }

        } catch (Exception e) {
            // 🛑 CATCH ALL EXCEPTIONS
            log.error("❌ Error processing soutenance event", e);
        }
    }
}