package com.devbuild.notificationservice.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;

@Service
public class PdfGeneratorService {

    public byte[] generateAttestationInscription(String studentName, String academicYear) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);

            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Paragraph title = new Paragraph("ATTESTATION D'INSCRIPTION", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph("\n\n"));

            Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            document.add(new Paragraph("Nous soussignés, administration du CED, certifions que :", contentFont));
            document.add(new Paragraph("\nNom et Prénom : " + studentName, FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            document.add(new Paragraph("\nEst régulièrement inscrit(e) en Doctorat pour l'année universitaire : " + academicYear, contentFont));

            document.add(new Paragraph("\n\nFait le : " + LocalDate.now()));
            document.add(new Paragraph("\n\nSignature de l'administration"));

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error creating PDF", e);
        }
    }


    public byte[] generateAutorisationSoutenance(String studentName, String date, String time, String location) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("AUTORISATION DE SOUTENANCE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph("\n\n"));
            document.add(new Paragraph("Le candidat : " + studentName));
            document.add(new Paragraph("Est autorisé à soutenir sa thèse."));
            document.add(new Paragraph("\nDate : " + (date != null ? date : "À définir")));
            document.add(new Paragraph("Heure : " + (time != null ? time : "--:--")));
            document.add(new Paragraph("Lieu : " + (location != null ? location : "À définir")));

            document.add(new Paragraph("\n\nSignature du Doyen"));
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }
}