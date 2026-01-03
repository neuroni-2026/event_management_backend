package ro.proiect.event_management.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ro.proiect.event_management.dto.response.MessageResponse;
import ro.proiect.event_management.security.services.UserDetailsImpl;
import ro.proiect.event_management.service.EventService;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/materials")
@Tag(name = "Materiale", description = "Gestionare materiale/atașamente evenimente")
public class MaterialController
{
    @Autowired
    private EventService eventService;

    // 1. ADAUGA MATERIALE LA EVENIMENT EXISTENT
    @PostMapping(value = "/event/{eventId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary = "Adaugă materiale la un eveniment existent")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Materiale adăugate cu succes"),
            @ApiResponse(responseCode = "403", description = "Nu ai permisiunea (nu e evenimentul tău)"),
            @ApiResponse(responseCode = "404", description = "Evenimentul nu există")
    })
    public ResponseEntity<?> addMaterialsToEvent(
            @PathVariable Long eventId,
            @Parameter(description = "Lista de fișiere (PDF, Imagini, etc.)")
            @RequestPart("files") List<MultipartFile> files
    )
    {
        try
        {
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            eventService.addMaterials(eventId, userDetails.getId(), files);

            return ResponseEntity.ok(new MessageResponse("Materials added successfully!"));
        }
        catch (RuntimeException e)
        {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    // 2. STERGE UN MATERIAL SPECIFIC
    @DeleteMapping("/{materialId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary = "Șterge un material specific")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Material șters cu succes"),
            @ApiResponse(responseCode = "403", description = "Nu ai permisiunea"),
            @ApiResponse(responseCode = "404", description = "Materialul nu există")
    })
    public ResponseEntity<?> deleteMaterial(@PathVariable Long materialId)
    {
        try
        {
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            eventService.deleteMaterial(materialId, userDetails.getId());

            return ResponseEntity.ok(new MessageResponse("Material deleted successfully!"));
        }
        catch (RuntimeException e)
        {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/download/{materialId}")
    @Operation(summary = "Descarcă un material prin proxy server")
    public ResponseEntity<org.springframework.core.io.Resource> downloadMaterial(@PathVariable Long materialId)
    {
        try
        {
            var material = eventService.getMaterialById(materialId);
            String urlString = material.getFileUrl();
            java.io.InputStream in;
            
            try
            {
                in = openConnection(urlString);
            }
            catch (Exception e)
            {
                System.err.println("Primary download failed for: " + urlString + " Error: " + e.getMessage());
                // Fallback logic
                String altUrlString;
                if (urlString.contains("/raw/upload/")) {
                    altUrlString = urlString.replace("/raw/upload/", "/image/upload/");
                } else if (urlString.contains("/image/upload/")) {
                    altUrlString = urlString.replace("/image/upload/", "/raw/upload/");
                } else {
                    throw e;
                }
                
                System.out.println("Retry download with URL: " + altUrlString);
                in = openConnection(altUrlString);
            }

            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + material.getFileName() + "\"")
                    .body(new org.springframework.core.io.InputStreamResource(in));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    private java.io.InputStream openConnection(String urlString) throws java.io.IOException
    {
        java.net.URL url = new java.net.URL(urlString);
        java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
        // Setăm User-Agent pentru a nu fi blocați de Cloudinary
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
        return connection.getInputStream();
    }
}
