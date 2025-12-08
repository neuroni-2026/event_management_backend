package ro.proiect.event_management.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ro.proiect.event_management.dto.response.FavoriteResponse;
import ro.proiect.event_management.dto.response.MessageResponse;
import ro.proiect.event_management.security.services.UserDetailsImpl;
import ro.proiect.event_management.service.FavoriteService;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/favorites")
@Tag(name = "Favorite", description = "Gestionare listă evenimente favorite")
public class FavoriteController
{
    @Autowired
    private FavoriteService favoriteService;

    // Helper: Extrage ID-ul userului logat
    private Long getLoggedUserId() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getId();
    }

    // 1. ADAUGA LA FAVORITE
    // POST /api/favorites/10
    @PostMapping("/{eventId}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Adaugă eveniment la favorite")
    public ResponseEntity<?> addFavorite(@PathVariable Long eventId)
    {
        try
        {
            favoriteService.addFavorite(eventId, getLoggedUserId());
            return ResponseEntity.ok(new MessageResponse("Added to favorites!"));
        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    // 2. STERGE DIN FAVORITE
    // DELETE /api/favorites/10
    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Șterge eveniment din favorite")
    public ResponseEntity<?> removeFavorite(@PathVariable Long eventId)
    {
        try
        {
            favoriteService.removeFavorite(eventId, getLoggedUserId());
            return ResponseEntity.ok(new MessageResponse("Removed from favorites!"));
        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    // 3. LISTA MEA
    // GET /api/favorites
    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Vezi lista mea de favorite")
    public List<FavoriteResponse> getMyFavorites()
    {
        return favoriteService.getMyFavorites(getLoggedUserId());
    }

    // 4. VERIFICA DACA E FAVORIT (Pt UI - inimioara plina/goala)
    // GET /api/favorites/check/10
    @GetMapping("/check/{eventId}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Verifică dacă un eveniment este favorit")
    public ResponseEntity<Boolean> checkFavorite(@PathVariable Long eventId)
    {
        boolean isFav = favoriteService.isFavorite(eventId, getLoggedUserId());
        return ResponseEntity.ok(isFav);
    }

}
