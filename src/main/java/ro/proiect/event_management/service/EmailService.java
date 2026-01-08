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
        // Culori extrase din frontend (Tailwind/CSS variables)
        String primaryColor = "#ff6b6b";    // hsl(0, 100%, 71%) - Salmon
        String primaryDark = "#ea580c";     // Orange-600
        String accentColor = "#8b5cf6";     // hsl(262, 83%, 58%) - Violet
        String bgColor = "#f9fafb";         // Background deschis
        String cardBg = "#ffffff";
        String darkText = "#111827";        // Gray-900
        String mutedText = "#6b7280";       // Gray-500
        String lightBorder = "#f3f4f6";     // Gray-100
        String orangeBg = "#fff7ed";        // Orange-50 (Used as soft background for accent)

        String displayCode = (code != null) ? code.substring(0, Math.min(code.length(), 8)) : "N/A";

        return "" +
                "<div style=\"font-family: 'Inter', 'Segoe UI', Roboto, sans-serif; background-color: " + bgColor + "; padding: 40px 20px; text-align: center;\">" +
                "  <div style=\"max-width: 450px; margin: 0 auto; background-color: " + cardBg + "; border-radius: 32px; overflow: hidden; box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.1); border: 1px solid " + lightBorder + ";\">" +
                "    " +
                "    <!-- Header Gradient Section -->" +
                "    <div style=\"background: linear-gradient(135deg, " + primaryColor + " 0%, " + primaryDark + " 100%); background-color: " + primaryColor + "; padding: 40px 30px; color: #ffffff;\">" +
                "      <div style=\"display: inline-block; background-color: rgba(255,255,255,0.2); padding: 6px 16px; border-radius: 100px; border: 1px solid rgba(255,255,255,0.3); margin-bottom: 20px;\">" +
                "        <span style=\"font-size: 11px; font-weight: 800; text-transform: uppercase; letter-spacing: 2px;\">✓ Bilet Valid</span>" +
                "      </div>" +
                "      <h1 style=\"margin: 0; font-size: 28px; font-weight: 900; line-height: 1.2; letter-spacing: -0.5px;\">" + title + "</h1>" +
                "      <p style=\"margin: 10px 0 0 0; font-size: 14px; opacity: 0.9; font-weight: 500;\">ID Bilet: #" + displayCode + "</p>" +
                "    </div>" +
                "" +
                "    <!-- QR Section -->" +
                "    <div style=\"padding: 40px 30px 20px 30px; background-color: " + cardBg + ";\">" +
                "      <div style=\"display: inline-block; padding: 12px; background-color: " + cardBg + "; border: 4px solid " + darkText + "; border-radius: 20px; box-shadow: 0 4px 10px rgba(0,0,0,0.05);\">" +
                "        <img src=\"cid:qr_code\" alt=\"QR Code\" width=\"180\" height=\"180\" style=\"display: block;\"/>" +
                "      </div>" +
                "      <p style=\"margin: 20px 0 30px 0; font-size: 10px; font-weight: 800; color: " + mutedText + "; text-transform: uppercase; letter-spacing: 3px;\">Scanează la intrare</p>" +
                "      " +
                "      <!-- Dotted Line Separator -->" +
                "      <div style=\"border-top: 2px dashed #e5e7eb; height: 1px; width: 100%; margin-bottom: 30px;\"></div>" +
                "    </div>" +
                "" +
                "    <!-- Details Section -->" +
                "    <div style=\"padding: 0 40px 40px 40px; text-align: left;\">" +
                "      " +
                "      <!-- Row 1: Date -->" +
                "      <table style=\"width: 100%; margin-bottom: 25px;\">" +
                "        <tr>" +
                "          <td style=\"width: 50px; vertical-align: top;\">" +
                "            <div style=\"background-color: " + orangeBg + "; color: " + accentColor + "; width: 44px; height: 44px; border-radius: 14px; text-align: center; line-height: 44px; font-size: 20px;\">📅</div>" +
                "          </td>" +
                "          <td style=\"padding-left: 15px;\">" +
                "            <p style=\"margin: 0; font-size: 10px; font-weight: 800; color: " + mutedText + "; text-transform: uppercase; letter-spacing: 1.5px;\">Data și Ora</p>" +
                "            <p style=\"margin: 2px 0 0 0; font-size: 16px; font-weight: 700; color: " + darkText + ";\">" + date + "</p>" +
                "          </td>" +
                "        </tr>" +
                "      </table>" +
                "" +
                "      <!-- Row 2: Location -->" +
                "      <table style=\"width: 100%; margin-bottom: 25px;\">" +
                "        <tr>" +
                "          <td style=\"width: 50px; vertical-align: top;\">" +
                "            <div style=\"background-color: " + orangeBg + "; color: " + accentColor + "; width: 44px; height: 44px; border-radius: 14px; text-align: center; line-height: 44px; font-size: 20px;\">📍</div>" +
                "          </td>" +
                "          <td style=\"padding-left: 15px;\">" +
                "            <p style=\"margin: 0; font-size: 10px; font-weight: 800; color: " + mutedText + "; text-transform: uppercase; letter-spacing: 1.5px;\">Locație</p>" +
                "            <p style=\"margin: 2px 0 0 0; font-size: 16px; font-weight: 700; color: " + darkText + ";\">" + location + "</p>" +
                "          </td>" +
                "        </tr>" +
                "      </table>" +
                "" +
                "      <!-- Row 3: Participant -->" +
                "      <table style=\"width: 100%;\">" +
                "        <tr>" +
                "          <td style=\"width: 50px; vertical-align: top;\">" +
                "            <div style=\"background-color: " + orangeBg + "; color: " + accentColor + "; width: 44px; height: 44px; border-radius: 14px; text-align: center; line-height: 44px; font-size: 20px;\">👤</div>" +
                "          </td>" +
                "          <td style=\"padding-left: 15px;\">" +
                "            <p style=\"margin: 0; font-size: 10px; font-weight: 800; color: " + mutedText + "; text-transform: uppercase; letter-spacing: 1.5px;\">Participant</p>" +
                "            <p style=\"margin: 2px 0 0 0; font-size: 16px; font-weight: 700; color: " + darkText + ";\">" + name + "</p>" +
                "          </td>" +
                "        </tr>" +
                "      </table>" +
                "" +
                "    </div>" +
                "" +
                "    <!-- Bottom Branding -->" +
                "    <div style=\"background-color: " + bgColor + "; padding: 25px; border-top: 1px solid " + lightBorder + ";\">" +
                "      <p style=\"margin: 0; font-size: 11px; font-weight: 700; color: " + mutedText + "; text-transform: uppercase; letter-spacing: 2px;\">EventManager Platform</p>" +
                "    </div>" +
                "  </div>" +
                "  " +
                "  <p style=\"margin-top: 30px; font-size: 11px; color: #94a3b8;\">Acesta este un bilet electronic validat prin sistemul nostru.<br/>Te rugăm să nu distribui acest email.</p>" +
                "</div>";
    }
}