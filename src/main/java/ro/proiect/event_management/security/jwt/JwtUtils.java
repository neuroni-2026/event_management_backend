package ro.proiect.event_management.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import ro.proiect.event_management.security.services.UserDetailsImpl;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils
{
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    // Citim valorile din application.properties
    @Value("${security.jwt.secret-key}")
    private String jwtSecret;

    @Value("${security.jwt.expiration-time}")
    private int jwtExpirationMs;

    // Metoda care transforma String-ul secret intr-o Cheie criptografica
    private Key key()
    {
        // Varianta sigura pentru string-uri simple (cum am pus noi in properties)
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // 1. GENERARE TOKEN
    public String generateJwtToken(Authentication authentication)
    {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject((userPrincipal.getUsername())) // Punem email-ul in token
                .setIssuedAt(new Date()) // Data crearii
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs)) // Data expirarii
                .signWith(key(), SignatureAlgorithm.HS256) // Semnatura digitala
                .compact();
    }

    // 2. EXTRAGERE USERNAME DIN TOKEN
    public String getUserNameFromJwtToken(String token)
    {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    // 3. VALIDARE TOKEN
    public boolean validateJwtToken(String authToken)
    {
        try
        {
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken);
            return true;
        }
        catch (SecurityException e)
        {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        }
        catch (MalformedJwtException e)
        {
            logger.error("Invalid JWT token: {}", e.getMessage());
        }
        catch (ExpiredJwtException e)
        {
            logger.error("JWT token is expired: {}", e.getMessage());
        }
        catch (UnsupportedJwtException e)
        {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        }
        catch (IllegalArgumentException e)
        {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }
}