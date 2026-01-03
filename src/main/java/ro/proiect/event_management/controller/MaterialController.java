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
    }
