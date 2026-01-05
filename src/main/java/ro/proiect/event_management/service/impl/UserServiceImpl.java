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

    @Autowired
    ro.proiect.event_management.service.EmailService emailService;

    @Autowired
    ro.proiect.event_management.service.NotificationService notificationService;


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


        // 3. Setam Rolurile - TOATA LUMEA E STUDENT LA INCEPUT
        user.setRole(UserRole.STUDENT);
        user.setPendingUpgradeRequest(false); // Default
        
        // Luam datele de student daca exista
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

    @Override
    public void requestOrganizerUpgrade(Long userId, String organizationName, String reason)
    {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPendingUpgradeRequest(true);
        user.setPendingOrganizationName(organizationName);
        user.setPendingReason(reason);
        userRepository.save(user);
    }

    @Override
    public java.util.List<User> getOrganizerRequests()
    {
        return userRepository.findPendingOrganizerRequests();
    }

    @Override
    public java.util.List<ro.proiect.event_management.dto.response.OrganizerStatsDto> getOrganizerStats() {
        return userRepository.getOrganizerStats();
    }

    @Override
    public void suspendUser(Long userId, Integer days) {
        User user = getUserById(userId);
        user.setSuspendedUntil(java.time.LocalDateTime.now().plusDays(days));
        userRepository.save(user);
        emailService.sendSimpleEmail(user.getEmail(), "Cont Suspendat", "Contul tău a fost suspendat pentru " + days + " zile.");
    }

    @Override
    public void unsuspendUser(Long userId) {
        User user = getUserById(userId);
        user.setSuspendedUntil(null);
        userRepository.save(user);
        emailService.sendSimpleEmail(user.getEmail(), "Suspendare Anulată", "Suspendarea contului a fost ridicată.");
    }

    @Override
    public void banUser(Long userId) {
        User user = getUserById(userId);
        
        // Toggle Logic
        boolean newStatus = !Boolean.TRUE.equals(user.getIsEnabled());
        user.setIsEnabled(newStatus);
        
        userRepository.save(user);
        
        if (newStatus) {
             emailService.sendSimpleEmail(user.getEmail(), "Cont Deblocat", "Contul tău a fost deblocat. Te rugăm să respecți regulile.");
        } else {
             emailService.sendSimpleEmail(user.getEmail(), "Cont Blocat", "Contul tău a fost blocat permanent pentru încălcarea regulilor.");
        }
    }

    @Override
    public void downgradeUser(Long userId) {
        User user = getUserById(userId);
        user.setRole(UserRole.STUDENT);
        user.setOrganizationName(null);
        userRepository.save(user);
        emailService.sendSimpleEmail(user.getEmail(), "Retragere Drepturi", "Drepturile tale de organizator au fost revocate.");
    }

    @Override
    public java.util.List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void approveOrganizer(Long userId)
    {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(UserRole.ORGANIZER);
        user.setOrganizationName(user.getPendingOrganizationName());
        user.setPendingOrganizationName(null);
        user.setPendingReason(null);
        user.setPendingUpgradeRequest(false);
        
        userRepository.save(user);
        emailService.sendSimpleEmail(user.getEmail(), "Cerere Aprobată", "Felicitări! Cererea ta de organizator a fost aprobată.");

        // Notificare in-app
        ro.proiect.event_management.entity.Notification notification = ro.proiect.event_management.entity.Notification.builder()
                .user(user)
                .type(ro.proiect.event_management.entity.NotificationType.INFO)
                .message("Felicitări! Cererea ta de organizator a fost aprobată. Acum poți crea evenimente.")
                .isRead(false)
                .build();
        notificationService.createNotification(notification);
    }

    @Override
    public void rejectOrganizer(Long userId, String reason)
    {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPendingOrganizationName(null);
        user.setPendingReason(null);
        user.setPendingUpgradeRequest(false);

        userRepository.save(user);
        emailService.sendSimpleEmail(user.getEmail(), "Cerere Respinsă", "Cererea ta a fost respinsă. Motiv: " + reason);

        // Notificare in-app
        ro.proiect.event_management.entity.Notification notification = ro.proiect.event_management.entity.Notification.builder()
                .user(user)
                .type(ro.proiect.event_management.entity.NotificationType.INFO)
                .message("Cererea ta de organizator a fost respinsă. Motiv: " + reason)
                .isRead(false)
                .build();
        notificationService.createNotification(notification);
    }
}