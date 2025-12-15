package ro.proiect.event_management.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Component;
import java.io.ByteArrayOutputStream;

@Component
public class QRCodeGenerator
{
    public byte[] generateQRCodeImage(String text, int width, int height)
    {
        try
        {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);

            return pngOutputStream.toByteArray();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Eroare generare QR Code: " + e.getMessage());
        }
    }
}
