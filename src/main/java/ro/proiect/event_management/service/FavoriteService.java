package ro.proiect.event_management.service;

import ro.proiect.event_management.dto.response.FavoriteResponse;

import java.util.List;

public interface FavoriteService
{
    void addFavorite(Long eventId, Long userId);
    void removeFavorite(Long eventId, Long userId);
    List<FavoriteResponse> getMyFavorites(Long userId);
    boolean isFavorite(Long eventId, Long userId); // Pt a colora inimioara in UI
}
