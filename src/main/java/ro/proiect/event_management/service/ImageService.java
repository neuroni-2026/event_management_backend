package ro.proiect.event_management.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class ImageService {

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
}