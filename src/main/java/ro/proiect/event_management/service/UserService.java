package ro.proiect.event_management.service;

import ro.proiect.event_management.dto.request.ChangePasswordRequest;
import ro.proiect.event_management.dto.request.SignupRequest;
import ro.proiect.event_management.dto.request.UpdateProfileRequest;
import ro.proiect.event_management.dto.response.OrganizerStatsDto;
import ro.proiect.event_management.entity.User;

import java.util.List;

public interface UserService
{
    // Metoda care preia toata logica de inregistrare
    void registerUser(SignupRequest signUpRequest);

    User updateProfile(Long userId, UpdateProfileRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);

    User getUserById(Long userId);

    void requestOrganizerUpgrade(Long userId, String organizationName, String reason);

    List<User> getOrganizerRequests();

    void approveOrganizer(Long userId);

    void rejectOrganizer(Long userId, String reason);

    // --- Analytics & Moderation ---
    List<OrganizerStatsDto> getOrganizerStats();
    void suspendUser(Long userId, Integer days);
    void unsuspendUser(Long userId);
    void banUser(Long userId);
    void downgradeUser(Long userId);

    List<User> getAllUsers();
}