package ro.proiect.event_management.config;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig
{
    @Bean
    public Cloudinary cloudinary()
    {
        Map<String, String> config = new HashMap<>();

        config.put("cloud_name", "dozehb22z");
        config.put("api_key", "239485667242298");
        config.put("api_secret", "fqppayo_kCg4E4Nrb-nA_Q2f0bc");

        return new Cloudinary(config);
    }
}
