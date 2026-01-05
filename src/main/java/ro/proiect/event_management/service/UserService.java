package ro.proiect.event_management.service;

import ro.proiect.event_management.dto.request.ChangePasswordRequest;
import ro.proiect.event_management.dto.request.SignupRequest;
import ro.proiect.event_management.dto.request.UpdateProfileRequest;
import ro.proiect.event_management.entity.User;

public interface UserService
{
    // Metoda care preia toata logica de inregistrare
    void registerUser(SignupRequest signUpRequest);

    User updateProfile(Long userId, UpdateProfileRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);

    User getUserById(Long userId);

    void requestOrganizerUpgrade(Long userId, String organizationName, String reason);

    java.util.List<User> getOrganizerRequests();

    void approveOrganizer(Long userId);

    void rejectOrganizer(Long userId, String reason);

    // --- Analytics & Moderation ---
    java.util.List<ro.proiect.event_management.dto.response.OrganizerStatsDto> getOrganizerStats();
    void suspendUser(Long userId, Integer days);
    void unsuspendUser(Long userId);
    void banUser(Long userId);
    void downgradeUser(Long userId);

    java.util.List<User> getAllUsers();
}