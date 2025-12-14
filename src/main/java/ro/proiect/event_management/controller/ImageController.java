package ro.proiect.event_management.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ro.proiect.event_management.service.ImageService;

import java.util.Map;

@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Imagini", description = "Încărcare imagini (Cloudinary)")
public class ImageController
{

    @Autowired
    private ImageService imageService;

    // Endpoint pentru incarcarea imaginii
    // Frontend-ul trimite un fisier, Backend-ul raspunde cu un URL
    @PostMapping("/upload")
    @Operation(summary = "Încarcă o imagine și returnează URL-ul")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Imagine încărcată cu succes"),
            @ApiResponse(responseCode = "400", description = "Eroare la încărcare")
    })
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file)
    {
        try
        {
            String url = imageService.uploadImage(file);

            // Returnam un JSON simplu: { "url": "https://..." }
            return ResponseEntity.ok(Map.of("url", url));

        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body("Eroare la upload: " + e.getMessage());
        }
    }
}