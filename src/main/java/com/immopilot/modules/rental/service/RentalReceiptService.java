package com.immopilot.modules.rental.service;

import com.immopilot.modules.properties.domain.Property;
import com.immopilot.modules.rental.domain.Lease;
import com.immopilot.modules.rental.domain.RentPayment;
import com.immopilot.modules.rental.domain.Tenant;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RentalReceiptService {

    private static final Font FONT_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
    private static final Font FONT_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font FONT_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 12);

    public byte[] generateReceipt(RentPayment payment) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            Lease lease = payment.getLease();
            Property property = lease.getProperty();
            Set<Tenant> tenants = lease.getTenants();

            // Title
            Paragraph title = new Paragraph("QUITTANCE DE LOYER", FONT_TITLE);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Period
            String period = payment.getPaymentDate().format(DateTimeFormatter.ofPattern("MMMM yyyy"));
            Paragraph periodPara = new Paragraph("Période : " + period, FONT_HEADER);
            periodPara.setAlignment(Element.ALIGN_CENTER);
            periodPara.setSpacingAfter(20);
            document.add(periodPara);

            // Owner Info (Mocked or Generic for now if User details complex to access)
            // Ideally we get this from property.getUser()
            Paragraph owner = new Paragraph("BAILLEUR:\n" + (property.getUser() != null
                    ? property.getUser().getFirstName() + " " + property.getUser().getLastName()
                    : "Propriétaire"), FONT_NORMAL);
            owner.setSpacingAfter(10);
            document.add(owner);

            // Tenant Info
            String tenantNames = tenants.stream()
                    .map(t -> t.getFirstName() + " " + t.getLastName())
                    .collect(Collectors.joining(", "));
            Paragraph tenant = new Paragraph("LOCATAIRE(S):\n" + tenantNames, FONT_NORMAL);
            tenant.setSpacingAfter(10);
            document.add(tenant);

            // Property Address
            Paragraph propertyPara = new Paragraph(
                    "LOGEMENT:\n" + property.getAddress() + ", " + property.getZipCode() + " " + property.getCity(),
                    FONT_NORMAL);
            propertyPara.setSpacingAfter(20);
            document.add(propertyPara);

            // Payment Details
            document.add(new Paragraph("Détail du paiement :", FONT_HEADER));
            document.add(new Paragraph("Montant du loyer : " + payment.getAmount() + " EUR", FONT_NORMAL));
            document.add(new Paragraph(
                    "Date de paiement : " + payment.getPaymentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    FONT_NORMAL));
            document.add(new Paragraph(
                    "Moyen de paiement : "
                            + (payment.getPaymentType() != null ? payment.getPaymentType().toString() : "Non spécifié"),
                    FONT_NORMAL));

            Paragraph status = new Paragraph("\nStatut : " + payment.getStatus(), FONT_HEADER);
            document.add(status);

            // Footer
            Paragraph footer = new Paragraph("\n\nPour valoir ce que de droit.", FONT_NORMAL);
            footer.setAlignment(Element.ALIGN_RIGHT);
            document.add(footer);

            Paragraph date = new Paragraph(
                    "Fait le " + java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    FONT_NORMAL);
            date.setAlignment(Element.ALIGN_RIGHT);
            document.add(date);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error generating receipt PDF", e);
            throw new RuntimeException("Could not generate receipt PDF", e);
        }
    }
}
