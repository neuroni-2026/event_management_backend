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

    //@Value("${MAIL_USERNAME}")
    private String apiKey="00ea14d153767da0b960a7caa8d62063";

    //@Value("${MAIL_PASSWORD}")
    private String secretKey="96542abbe388cfb873ee58dce15615dd";

    @Async
    public void sendSimpleEmail(String toEmail, String subject, String textContent) {
        try {
            ClientOptions options = ClientOptions.builder()
                    .apiKey(apiKey)
                    .apiSecretKey(secretKey)
                    .build();

            MailjetClient client = new MailjetClient(options);

            MailjetRequest request = new MailjetRequest(Emailv31.resource)
                    .property(Emailv31.MESSAGES, new JSONArray()
                            .put(new JSONObject()
                                    .put(Emailv31.Message.FROM, new JSONObject()
                                            .put("Email", "bogdan.rusu1@student.usv.ro")
                                            .put("Name", "Event Management"))
                                    .put(Emailv31.Message.TO, new JSONArray()
                                            .put(new JSONObject()
                                                    .put("Email", toEmail)))
                                    .put(Emailv31.Message.SUBJECT, subject)
                                    .put(Emailv31.Message.TEXTPART, textContent)
                            ));

            client.post(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
                "<div style=\"font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 600px; margin: 0 auto; background-color: #f8fafc; padding: 40px 20px;\">" +
                "  <div style=\"background-color: #ffffff; border-radius: 32px; overflow: hidden; box-shadow: 0 20px 40px -10px rgba(0,0,0,0.1); border: 1px solid #e2e8f0;\">" +
                "    <!-- Branding Header -->" +
                "    <div style=\"background-color: #ffffff; padding: 25px; text-align: center; border-bottom: 1px solid #f1f5f9;\">" +
                "      <h2 style=\"color: #1e293b; margin: 0; font-weight: 900; letter-spacing: -1px; text-transform: uppercase; font-style: italic; font-size: 20px;\">EventManager</h2>" +
                "    </div>" +
                "    " +
                "    <!-- Hero Section (Event Title) -->" +
                "    <div style=\"background-color: #2563eb; padding: 45px 35px; text-align: center;\">" +
                "      <span style=\"display: inline-block; background-color: rgba(255,255,255,0.2); color: white; padding: 5px 15px; border-radius: 100px; font-size: 10px; font-weight: 800; text-transform: uppercase; letter-spacing: 1.5px; margin-bottom: 20px;\">Bilet Confirmat</span>" +
                "      <h1 style=\"color: white; margin: 0; font-size: 32px; line-height: 1.2; font-weight: 800; text-shadow: 0 2px 4px rgba(0,0,0,0.1);\">" + title + "</h1>" +
                "    </div>" +
                "" +
                "    <!-- Body Content -->" +
                "    <div style=\"padding: 40px 35px;\">" +
                "      <p style=\"color: #64748b; font-size: 16px; margin-bottom: 30px; text-align: center;\">Salut <strong>" + name + "</strong>, locul tău a fost rezervat cu succes!</p>" +
                "      " +
                "      <div style=\"background-color: #f8fafc; border-radius: 24px; padding: 30px; margin-bottom: 35px; border: 1px solid #f1f5f9;\">" +
                "        <table style=\"width: 100%; border-collapse: collapse;\">" +
                "          <tr>" +
                "            <td style=\"padding-bottom: 25px;\">" +
                "              <p style=\"color: #94a3b8; font-size: 10px; font-weight: 800; text-transform: uppercase; letter-spacing: 1.5px; margin: 0 0 8px 0;\">📍 UNDE</p>" +
                "              <p style=\"color: #1e293b; font-size: 16px; font-weight: 700; margin: 0;\">" + location + "</p>" +
                "            </td>" +
                "          </tr>" +
                "          <tr>" +
                "            <td>" +
                "              <p style=\"color: #94a3b8; font-size: 10px; font-weight: 800; text-transform: uppercase; letter-spacing: 1.5px; margin: 0 0 8px 0;\">📅 CÂND</p>" +
                "              <p style=\"color: #1e293b; font-size: 16px; font-weight: 700; margin: 0;\">" + date + "</p>" +
                "            </td>" +
                "          </tr>" +
                "        </table>" +
                "      </div>" +
                "" +
                "      <!-- Access Section -->" +
                "      <div style=\"text-align: center; padding: 30px; background-color: #ffffff; border: 2px dashed #e2e8f0; border-radius: 28px;\">" +
                "        <p style=\"color: #1e293b; font-size: 12px; font-weight: 800; margin-bottom: 20px; text-transform: uppercase; letter-spacing: 2px;\">COD ACCES: " + code + "</p>" +
                "        <img src=\"cid:qr_code\" alt=\"QR Code\" width=\"200\" height=\"200\" style=\"display: block; margin: 0 auto; border-radius: 12px;\"/>" +
                "        <p style=\"color: #94a3b8; font-size: 12px; margin-top: 20px; font-weight: 500;\">Prezintă acest cod la intrare pentru scanare.</p>" +
                "      </div>" +
                "    </div>" +
                "" +
                "    <!-- Professional Footer -->" +
                "    <div style=\"background-color: #f8fafc; padding: 25px; text-align: center; border-top: 1px solid #f1f5f9;\">" +
                "      <p style=\"color: #94a3b8; font-size: 11px; margin: 0; font-weight: 600; text-transform: uppercase; letter-spacing: 1px;\">© 2026 EventManager Platform</p>" +
                "    </div>" +
                "  </div>" +
                "  <p style=\"text-align: center; color: #cbd5e1; font-size: 10px; margin-top: 20px;\">Acesta este un email automat. Te rugăm să nu răspunzi.</p>" +
                "</div>";
    }
}