package ro.proiect.event_management.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ro.proiect.event_management.util.QRCodeGenerator;

import java.io.UnsupportedEncodingException;

@Service
public class EmailService
{
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private QRCodeGenerator qrCodeGenerator;

    //@Value("${spring.mail.username}")
    private String fromEmail="bogdan.rusu1@student.usv.ro";

    @Async // Ruleaza pe alt thread
    public void sendTicketEmail(String toEmail, String userName, String eventTitle, String eventLocation, String eventDate, String ticketCode)
    {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            // true = multipart (pentru imagini inline)
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail,"EventManagement-NoReply");

            helper.setTo(toEmail);
            helper.setSubject("🎟️ Biletul tău pentru: " + eventTitle);

            // 1. Generam QR Code-ul
            byte[] qrImage = qrCodeGenerator.generateQRCodeImage(ticketCode, 200, 200);

            // 2. Construim HTML-ul
            String htmlContent = buildHtmlEmail(userName, eventTitle, eventLocation, eventDate, ticketCode);

            helper.setText(htmlContent, true);

            // 3. Atasam imaginea cu ID unic "qrCodeImage" folosit in HTML
            helper.addInline("qrCodeImage", new ByteArrayResource(qrImage), "image/png");

            mailSender.send(message);
            System.out.println("Email trimis cu succes catre: " + toEmail);

        }
        catch (MessagingException | UnsupportedEncodingException e)
        {
            System.err.println("Eroare la trimiterea emailului: " + e.getMessage());
        }
    }

    private String buildHtmlEmail(String name, String title, String location, String date, String code)
    {
        return "" +
                "<div style=\"font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; max-width: 600px; margin: 0 auto; background-color: #f4f4f4; padding: 20px;\">" +
                "  <div style=\"background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1);\">" +
                "    <div style=\"background-color: #4CAF50; padding: 20px; text-align: center;\">" +
                "      <h1 style=\"color: white; margin: 0; font-size: 24px;\">Confirmare Bilet</h1>" +
                "    </div>" +
                "    <div style=\"padding: 30px;\">" +
                "      <p style=\"font-size: 16px; color: #333;\">Salut <strong>" + name + "</strong>,</p>" +
                "      <p style=\"color: #666;\">Te așteptăm cu drag! Iată detaliile biletului tău:</p>" +
                "      <div style=\"background-color: #f9f9f9; border-left: 4px solid #4CAF50; padding: 15px; margin: 20px 0;\">" +
                "        <h2 style=\"margin-top: 0; color: #333; font-size: 20px;\">" + title + "</h2>" +
                "        <p style=\"margin: 5px 0;\">📍 <strong>Locație:</strong> " + location + "</p>" +
                "        <p style=\"margin: 5px 0;\">📅 <strong>Data:</strong> " + date + "</p>" +
                "        <p style=\"margin: 5px 0;\">🔢 <strong>Cod:</strong> " + code + "</p>" +
                "      </div>" +
                "      <div style=\"text-align: center; margin: 30px 0;\">" +
                "        <p style=\"font-size: 14px; color: #888; margin-bottom: 10px;\">Prezintă acest cod QR la intrare:</p>" +
                "        <img src=\"cid:qrCodeImage\" alt=\"QR Code\" style=\"border: 1px solid #ddd; padding: 5px; border-radius: 4px;\"/>" +
                "      </div>" +
                "    </div>" +
                "    <div style=\"background-color: #eeeeee; padding: 15px; text-align: center; font-size: 12px; color: #777;\">" +
                "      &copy; 2026 EventManagement. Toate drepturile rezervate." +
                "    </div>" +
                "  </div>" +
                "</div>";
    }

}
