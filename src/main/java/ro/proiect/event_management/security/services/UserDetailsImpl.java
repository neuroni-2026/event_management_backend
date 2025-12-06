package ro.proiect.event_management.security.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ro.proiect.event_management.entity.User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class UserDetailsImpl implements UserDetails
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private String username; // In cazul nostru va fi email-ul
    private String email;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long id, String username, String email, String password,
                           Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    // Aceasta metoda statica converteste User-ul din DB in UserDetails pentru Spring
    public static UserDetailsImpl build(User user)
    {
        // Convertim Rolul (ENUM) in Authority (Spring Security format)
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                // Ex: "ROLE_STUDENT"
        );

        return new UserDetailsImpl(
                user.getId(),
                user.getEmail(), // Folosim email pe post de username
                user.getEmail(),
                user.getPassword(),
                authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public Long getId()
    {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    // Contul nu expira niciodata in demo-ul nostru
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // Contul nu e blocat
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // Credentialele nu expira
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // Contul e activ
    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }
}
