package ro.proiect.event_management.service;

import ro.proiect.event_management.dto.request.SignupRequest;

public interface UserService
{
    // Metoda care preia toata logica de inregistrare
    void registerUser(SignupRequest signUpRequest);
}