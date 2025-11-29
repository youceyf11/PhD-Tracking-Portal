package com.devbuild.notificationservice.service;

import com.devbuild.notificationservice.client.SoutenanceServiceClient;
import com.devbuild.notificationservice.client.UserServiceClient;
import com.devbuild.notificationservice.dto.DemandeInfo;
import com.devbuild.notificationservice.dto.DossierStatusChangedEvent;
import com.devbuild.notificationservice.dto.UserResponse;

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

    /**
     * Handles Inscription Events (JSON Object)
     */
    @KafkaListener(topics = "dossier-status-changed", groupId = "notification-group")
    public void handleDossierEvent(DossierStatusChangedEvent event) {
        log.info("Event received: Dossier {} changed to {}", event.getDossierId(), event.getNouveauStatut());

        // Fetch User Info
        UserResponse user = userServiceClient.getUserById(event.getDoctorantId());
        if (user == null || user.getEmail() == null) {
            log.warn("User info not found for ID: {}", event.getDoctorantId());
            return;
        }

        String email = user.getEmail();
        String fullName = user.getPrenom() + " " + user.getNom();

        // 1. If Validated -> Send Attestation
        if ("VALIDE".equals(event.getNouveauStatut())) {
            log.info("Generating Attestation for {}", email);

            byte[] pdf = pdfService.generateAttestationInscription(fullName, String.valueOf(LocalDate.now().getYear()));

            String body = "<h1>Félicitations " + user.getPrenom() + "</h1>" +
                    "<p>Votre inscription a été validée par l'administration.</p>" +
                    "<p>Veuillez trouver ci-joint votre <b>Attestation d'inscription</b>.</p>";

            emailService.sendEmailWithAttachment(email, "Votre Attestation d'Inscription", body, pdf, "attestation_inscription.pdf");
        }

        // 2. If Rejected -> Send Notification
        else if ("REJETE".equals(event.getNouveauStatut())) {
            String body = "<h1>Bonjour " + user.getPrenom() + "</h1>" +
                    "<p>Votre dossier a été rejeté.</p>" +
                    "<p><b>Motif :</b> " + event.getCommentaire() + "</p>";

            emailService.sendEmail(email, "Mise à jour de votre dossier", body);
        }
    }

    /**
     * Handles Soutenance Events (Long ID)
     * Note: In a real scenario, we would need to fetch the 'DemandeSoutenance' details first
     * to get the doctorantId. For now, we log it.
     */
    @KafkaListener(topics = {"soutenance-authorisee", "demande-submitted", "soutenance-planified"}, groupId = "notification-group")
    public void handleSoutenanceEvent(ConsumerRecord<String, String> record) {
        String demandeIdStr = String.valueOf(record.value());
        String topic = record.topic();

        log.info("Soutenance Event received: Topic={}, ID={}", topic, demandeIdStr);

        try {
            Long demandeId = Long.parseLong(demandeIdStr);

            // 1. Fetch Demande Details (to get doctorantId)
            DemandeInfo demande = soutenanceClient.getDemandeInfo(demandeId);

            // 2. Fetch User Details (to get Email)
            UserResponse user = userServiceClient.getUserById(demande.getDoctorantId());

            if (user == null || user.getEmail() == null) {
                log.warn("User info not found for Doctorant ID: {}", demande.getDoctorantId());
                return;
            }

            String fullName = user.getPrenom() + " " + user.getNom();
            String email = user.getEmail();

            // 3. Handle specific topics
            switch (topic) {
                case "demande-submitted":
                    emailService.sendEmail(email, "Accusé de réception",
                            "<h1>Bonjour " + user.getPrenom() + "</h1>" +
                                    "<p>Votre demande de soutenance a bien été reçue. Elle est en cours de vérification des prérequis.</p>");
                    break;

                case "soutenance-authorisee":
                    // Generate PDF
                    byte[] pdf = pdfService.generateAutorisationSoutenance(fullName, "À planifier", null, null);

                    emailService.sendEmailWithAttachment(email, "Soutenance Autorisée !",
                            "<h1>Félicitations !</h1><p>Votre soutenance est autorisée. L'administration va bientôt planifier la date.</p>",
                            pdf, "autorisation_soutenance.pdf");
                    break;

                case "soutenance-planified":
                    String dateStr = (demande.getDateSoutenance() != null) ? demande.getDateSoutenance().toString() : "N/A";
                    String timeStr = (demande.getHeureSoutenance() != null) ? demande.getHeureSoutenance().toString() : "N/A";

                    byte[] pdfPlanif = pdfService.generateAutorisationSoutenance(fullName, dateStr, timeStr, demande.getLieuSoutenance());

                    emailService.sendEmailWithAttachment(email, "Convocation à la soutenance",
                            "<h1>Convocation</h1>" +
                                    "<p>Votre soutenance est prévue le <b>" + dateStr + "</b> à <b>" + timeStr + "</b>.</p>" +
                                    "<p>Lieu : " + demande.getLieuSoutenance() + "</p>",
                            pdfPlanif, "convocation.pdf");
                    break;
            }

        } catch (Exception e) {
            log.error("Error processing soutenance event", e);
        }
    }
}