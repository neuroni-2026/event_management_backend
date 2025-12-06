package ro.proiect.event_management.security.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.proiect.event_management.entity.User;
import ro.proiect.event_management.repository.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService
{

    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException
    {
        // Cautam userul dupa email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with email: " + email));

        // Il convertim folosind metoda build() scrisa mai sus
        return UserDetailsImpl.build(user);
    }
}