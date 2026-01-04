package ro.proiect.event_management.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ro.proiect.event_management.dto.request.ChangePasswordRequest;
import ro.proiect.event_management.dto.request.SignupRequest;
import ro.proiect.event_management.dto.request.UpdateProfileRequest;
import ro.proiect.event_management.entity.Faculty;
import ro.proiect.event_management.entity.User;
import ro.proiect.event_management.entity.UserRole;
import ro.proiect.event_management.repository.UserRepository;
import ro.proiect.event_management.service.UserService;

@Service
public class UserServiceImpl implements UserService
{
    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;


    @Override
    public void registerUser(SignupRequest signUpRequest)
    {
        // 1. Verificam daca emailul exista deja
        if (userRepository.existsByEmail(signUpRequest.getEmail()))
        {
            throw new RuntimeException("Error: Email is already in use!");
        }

        // 2. Cream userul
        User user = new User();
        user.setFirstName(signUpRequest.getFirstName());
        user.setLastName(signUpRequest.getLastName());
        user.setEmail(signUpRequest.getEmail());
        user.setPhoneNumber(signUpRequest.getPhoneNumber());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));


        // 3. Setam Rolurile
        String strRole = signUpRequest.getRole().toLowerCase();

        if (strRole.equals("admin"))
        {
            user.setRole(UserRole.ADMIN);
        }
        else if (strRole.equals("organizer"))
        {
            user.setRole(UserRole.ORGANIZER);
            user.setOrganizationName(signUpRequest.getOrganizationName());
        }
        else
        {
            // Default STUDENT
            user.setRole(UserRole.STUDENT);
            user.setStudentYear(signUpRequest.getYearOfStudy());

            if (signUpRequest.getFaculty() != null)
            {
                try
                {
                    user.setStudentFaculty(Faculty.valueOf(signUpRequest.getFaculty()));
                }
                catch (IllegalArgumentException e)
                {
                    // Ignoram facultatea invalida
                }
            }
        }

        // 4. Salvam in baza de date
        userRepository.save(user);

    }

    @Override
    public User updateProfile(Long userId, UpdateProfileRequest request)
    {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getFirstName() != null && !request.getFirstName().isEmpty()) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null && !request.getLastName().isEmpty()) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        // Update role specific fields if provided
        if (user.getRole() == UserRole.ORGANIZER && request.getOrganizationName() != null) {
            user.setOrganizationName(request.getOrganizationName());
        }

        if (user.getRole() == UserRole.STUDENT) {
            if (request.getFaculty() != null) {
                try {
                    user.setStudentFaculty(Faculty.valueOf(request.getFaculty()));
                } catch (IllegalArgumentException e) {
                    // Ignore invalid faculty
                }
            }
            if (request.getYearOfStudy() != null) {
                user.setStudentYear(request.getYearOfStudy());
            }
        }

        return userRepository.save(user);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request)
    {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!encoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Parola curentă este incorectă!");
        }

        user.setPassword(encoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
