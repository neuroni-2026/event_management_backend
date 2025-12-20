package ro.proiect.event_management.service;

import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.resource.Emailv31;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ro.proiect.event_management.util.QRCodeGenerator;

import java.util.Base64;

@Service
public class EmailService {

    @Autowired
    private QRCodeGenerator qrCodeGenerator;

    // Citim cheile API direct din variabilele de mediu Railway
    @Value("${MAIL_USERNAME}")
    private String apiKey;

    @Value("${MAIL_PASSWORD}")
    private String secretKey;

    @Async
    public void sendTicketEmail(String toEmail, String userName, String eventTitle, String eventLocation, String eventDate, String ticketCode) {
        try {
            // 1. Configurare Client Mailjet (API HTTP)
            ClientOptions options = ClientOptions.builder()
                    .apiKey(apiKey)
                    .apiSecretKey(secretKey)
                    .build();

            MailjetClient client = new MailjetClient(options);

            // 2. Generare QR Code si conversie in Base64 (pentru a-l pune direct in HTML)
            byte[] qrImage = qrCodeGenerator.generateQRCodeImage(ticketCode, 200, 200);
            String base64QRCode = Base64.getEncoder().encodeToString(qrImage);
            String qrImageSrc = "data:image/png;base64," + base64QRCode;

            // 3. Construire HTML
            String htmlContent = buildHtmlEmail(userName, eventTitle, eventLocation, eventDate, ticketCode, qrImageSrc);

            // 4. Construire Request Mailjet
            MailjetRequest request = new MailjetRequest(Emailv31.resource)
                    .property(Emailv31.MESSAGES, new JSONArray()
                            .put(new JSONObject()
                                    .put(Emailv31.Message.FROM, new JSONObject()
                                            .put("Email", "bogdan.rusu1@student.usv.ro") // ⚠️ PUNE ADRESA TA VALIDATA DE MAILJET AICI
                                            .put("Name", "Event Management"))
                                    .put(Emailv31.Message.TO, new JSONArray()
                                            .put(new JSONObject()
                                                    .put("Email", toEmail)
                                                    .put("Name", userName)))
                                    .put(Emailv31.Message.SUBJECT, "🎟️ Biletul tău pentru: " + eventTitle)
                                    .put(Emailv31.Message.HTMLPART, htmlContent)
                            ));

            // 5. Trimite (Nu se blocheaza pe Railway!)
            MailjetResponse response = client.post(request);

            if (response.getStatus() == 200) {
                System.out.println("Email trimis cu succes catre: " + toEmail);
            } else {
                System.err.println("Eroare Mailjet: " + response.getStatus() + " - " + response.getData());
            }

        } catch (Exception e) {
            System.err.println("Eroare critica la trimiterea emailului: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String buildHtmlEmail(String name, String title, String location, String date, String code, String qrImageSrc) {
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
                "        <img src=\"" + qrImageSrc + "\" alt=\"QR Code\" style=\"border: 1px solid #ddd; padding: 5px; border-radius: 4px; width: 200px; height: 200px;\"/>" +
                "      </div>" +
                "    </div>" +
                "    <div style=\"background-color: #eeeeee; padding: 15px; text-align: center; font-size: 12px; color: #777;\">" +
                "      &copy; 2026 EventManagement. Toate drepturile rezervate." +
                "    </div>" +
                "  </div>" +
                "</div>";
    }
}