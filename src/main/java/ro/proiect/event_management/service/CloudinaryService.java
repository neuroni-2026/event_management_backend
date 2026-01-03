package ro.proiect.event_management.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService
{

    @Autowired
    private Cloudinary cloudinary;

    // Metoda care primeste fisierul si returneaza URL-ul imaginii
    public String uploadImage(MultipartFile file) throws IOException
    {
        // Urcam fisierul pe Cloudinary
        // ObjectUtils.emptyMap() inseamna ca nu trimitem parametri extra
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());

        // Extragem URL-ul securizat (https) din raspunsul primit
        return uploadResult.get("secure_url").toString();
    }

    public String uploadFile(MultipartFile file)
    {
        try
        {
            String originalName = file.getOriginalFilename();
            String resourceType = "auto";
            boolean isRaw = false;

            if (originalName != null) {
                String lower = originalName.toLowerCase();
                // Verificare documente RAW + ARHIVE
                if (lower.endsWith(".doc") || 
                    lower.endsWith(".docx") || 
                    lower.endsWith(".txt") || 
                    lower.endsWith(".xls") ||
                    lower.endsWith(".xlsx") ||
                    lower.endsWith(".csv") ||
                    lower.endsWith(".ppt") ||
                    lower.endsWith(".pptx") ||
                    lower.endsWith(".pdf") ||
                    lower.endsWith(".zip") ||
                    lower.endsWith(".rar") ||
                    lower.endsWith(".7z") ||
                    lower.endsWith(".tar") ||
                    lower.endsWith(".gz")) {
                    resourceType = "raw";
                    isRaw = true;
                } 
                // Verificare VIDEO + AUDIO
                else if (lower.endsWith(".mp4") || 
                         lower.endsWith(".avi") || 
                         lower.endsWith(".mov") || 
                         lower.endsWith(".mkv") || 
                         lower.endsWith(".webm") || 
                         lower.endsWith(".flv") || 
                         lower.endsWith(".wmv") ||
                         lower.endsWith(".mp3") ||
                         lower.endsWith(".wav") ||
                         lower.endsWith(".ogg") ||
                         lower.endsWith(".aac") ||
                         lower.endsWith(".flac") ||
                         lower.endsWith(".m4a")) {
                    resourceType = "video";
                }
            }

            java.util.Map<String, Object> params = new java.util.HashMap<>();
            params.put("resource_type", resourceType);

            if (isRaw && originalName != null)
            {
                String base = originalName.contains(".") ? originalName.substring(0, originalName.lastIndexOf(".")) : originalName;
                String ext = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf(".")) : "";
                base = base.replaceAll("[^a-zA-Z0-9\\-_]", "_");
                String finalName = base + "_" + System.currentTimeMillis() + ext;
                
                params.put("public_id", finalName);
                params.put("unique_filename", false);
                params.put("use_filename", true);
            }
            else
            {
                params.put("use_filename", true);
                params.put("unique_filename", true);
            }

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
            return (String) uploadResult.get("secure_url");
        }
        catch (IOException e)
        {
            throw new RuntimeException("Eroare la încărcarea fișierului pe Cloudinary: " + e.getMessage());
        }
    }

    public void deleteFile(String fileUrl)
    {
        if (fileUrl == null || fileUrl.isEmpty()) return;
        try
        {
            String uri = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            
            String resourceType = "image"; // Default
            boolean isRaw = false;

            String lower = uri.toLowerCase();
            // Documente RAW + ARHIVE
            if (lower.endsWith(".doc") || lower.endsWith(".docx") || 
                lower.endsWith(".xls") || lower.endsWith(".xlsx") || 
                lower.endsWith(".ppt") || lower.endsWith(".pptx") || 
                lower.endsWith(".txt") || lower.endsWith(".csv") ||
                lower.endsWith(".pdf") || 
                lower.endsWith(".zip") || lower.endsWith(".rar") ||
                lower.endsWith(".7z")  || lower.endsWith(".tar") || 
                lower.endsWith(".gz")) {
                resourceType = "raw";
                isRaw = true;
            } 
            // Video + AUDIO
            else if (lower.endsWith(".mp4") || lower.endsWith(".avi") || 
                     lower.endsWith(".mov") || lower.endsWith(".mkv") || 
                     lower.endsWith(".webm") || lower.endsWith(".flv") || 
                     lower.endsWith(".wmv") ||
                     lower.endsWith(".mp3") || lower.endsWith(".wav") ||
                     lower.endsWith(".ogg") || lower.endsWith(".aac") ||
                     lower.endsWith(".flac") || lower.endsWith(".m4a")) {
                resourceType = "video";
            }

            String publicId;
            if (isRaw)
            {
                publicId = uri;
            }
            else
            {
                publicId = uri.contains(".") ? uri.substring(0, uri.lastIndexOf(".")) : uri;
            }

            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", resourceType));
            
        }
        catch (IOException e)
        {
            throw new RuntimeException("Eroare la ștergerea fișierului din Cloudinary: " + e.getMessage());
        }
    }
}