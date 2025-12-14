package ro.proiect.event_management.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.proiect.event_management.dto.response.FavoriteResponse;
import ro.proiect.event_management.entity.Event;
import ro.proiect.event_management.entity.Favorite;
import ro.proiect.event_management.entity.User;
import ro.proiect.event_management.repository.EventRepository;
import ro.proiect.event_management.repository.FavoriteRepository;
import ro.proiect.event_management.repository.UserRepository;
import ro.proiect.event_management.service.FavoriteService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteServiceImpl implements FavoriteService
{
    @Autowired
    FavoriteRepository favoriteRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    UserRepository userRepository;

    @Override
    public void addFavorite(Long eventId, Long userId)
    {
        // 1. Verificam daca e deja favorit (ca sa nu avem duplicate)
        if (favoriteRepository.existsByUserIdAndEventId(userId, eventId))
        {
            throw new RuntimeException("Event is already in favorites!");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        Favorite favorite = Favorite.builder()
                .user(user)
                .event(event)
                .build();

        favoriteRepository.save(favorite);
    }

    @Override
    public void removeFavorite(Long eventId, Long userId)
    {
        Favorite favorite = favoriteRepository.findByUserIdAndEventId(userId, eventId)
                .orElseThrow(() -> new RuntimeException("Favorite not found"));

        favoriteRepository.delete(favorite);
    }

    @Override
    public List<FavoriteResponse> getMyFavorites(Long userId)
    {
        List<Favorite> favorites = favoriteRepository.findByUserId(userId);

        return favorites.stream().map(fav -> new FavoriteResponse(
                fav.getId(),
                fav.getEvent().getId(),
                fav.getEvent().getTitle(),
                fav.getEvent().getLocation(),
                fav.getEvent().getStartTime(),
                fav.getEvent().getImageUrl()
        )).collect(Collectors.toList());
    }

    @Override
    public boolean isFavorite(Long eventId, Long userId)
    {
        return favoriteRepository.existsByUserIdAndEventId(userId, eventId);
    }
}
