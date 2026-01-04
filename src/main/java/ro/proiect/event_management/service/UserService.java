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
}