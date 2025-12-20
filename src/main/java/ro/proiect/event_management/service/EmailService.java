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
public class EmailService
{

    @Autowired
    private QRCodeGenerator qrCodeGenerator;

    @Value("${MAIL_USERNAME}")
    private String apiKey;

    @Value("${MAIL_PASSWORD}")
    private String secretKey;

    @Async
    public void sendTicketEmail(String toEmail, String userName, String eventTitle, String eventLocation, String eventDate, String ticketCode)
    {
        try
        {
            // 1. Configurare Client
            ClientOptions options = ClientOptions.builder()
                    .apiKey(apiKey)
                    .apiSecretKey(secretKey)
                    .build();

            MailjetClient client = new MailjetClient(options);

            // 2. Generare QR Code
            byte[] qrImage = qrCodeGenerator.generateQRCodeImage(ticketCode, 200, 200);

            // Convertim in Base64 CURAT (fara "data:image/png...") pentru atasament
            String base64QRCode = Base64.getEncoder().encodeToString(qrImage);

            // 3. HTML-ul foloseste "cid:qr_code" care face referire la atasament
            String htmlContent = buildHtmlEmail(userName, eventTitle, eventLocation, eventDate, ticketCode);

            // 4. Construire Request cu Atasament Inline
            MailjetRequest request = new MailjetRequest(Emailv31.resource)
                    .property(Emailv31.MESSAGES, new JSONArray()
                            .put(new JSONObject()
                                    .put(Emailv31.Message.FROM, new JSONObject()
                                            .put("Email", "bogdan.rusu1@student.usv.ro")
                                            .put("Name", "Event Management"))
                                    .put(Emailv31.Message.TO, new JSONArray()
                                            .put(new JSONObject()
                                                    .put("Email", toEmail)
                                                    .put("Name", userName)))
                                    .put(Emailv31.Message.SUBJECT, "🎟️ Biletul tău pentru: " + eventTitle)
                                    .put(Emailv31.Message.HTMLPART, htmlContent)
                                    // AICI ESTE SECRETUL PENTRU IMAGINE:
                                    .put(Emailv31.Message.INLINEDATTACHMENTS, new JSONArray()
                                            .put(new JSONObject()
                                                    .put("ContentType", "image/png")
                                                    .put("Filename", "qrcode.png")
                                                    .put("ContentID", "qr_code") // ID-ul folosit in HTML (cid:qr_code)
                                                    .put("Base64Content", base64QRCode)
                                            )
                                    )
                            ));

            MailjetResponse response = client.post(request);

            if (response.getStatus() == 200)
            {
                System.out.println("Email cu QR trimis cu succes catre: " + toEmail);
            }
            else
            {
                System.err.println("Eroare Mailjet: " + response.getStatus() + " " + response.getData());
            }

        }
        catch (Exception e)
        {
            System.err.println("Eroare critica email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String buildHtmlEmail(String name, String title, String location, String date, String code)
    {
        return "" +
                "<div style=\"font-family: Helvetica, Arial, sans-serif; max-width: 600px; margin: 0 auto; background-color: #f4f4f4; padding: 20px;\">" +
                "  <div style=\"background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1);\">" +
                "    <div style=\"background-color: #4CAF50; padding: 20px; text-align: center;\">" +
                "      <h1 style=\"color: white; margin: 0; font-size: 24px;\">Confirmare Bilet</h1>" +
                "    </div>" +
                "    <div style=\"padding: 30px;\">" +
                "      <p>Salut <strong>" + name + "</strong>,</p>" +
                "      <p>Te așteptăm cu drag! Iată detaliile:</p>" +
                "      <div style=\"background-color: #f9f9f9; border-left: 4px solid #4CAF50; padding: 15px; margin: 20px 0;\">" +
                "        <h3 style=\"margin-top: 0;\">" + title + "</h3>" +
                "        <p>📍 " + location + "</p>" +
                "        <p>📅 " + date + "</p>" +
                "        <p>🔢 Cod: " + code + "</p>" +
                "      </div>" +
                "      <div style=\"text-align: center; margin-top: 30px;\">" +
                "        <p style=\"color: #888; font-size: 14px;\">Prezintă acest cod QR la intrare:</p>" +
                "        <img src=\"cid:qr_code\" alt=\"QR Code\" width=\"200\" height=\"200\" style=\"border: 1px solid #ddd; padding: 5px; border-radius: 4px;\"/>" +
                "      </div>" +
                "    </div>" +
                "  </div>" +
                "</div>";
    }
}