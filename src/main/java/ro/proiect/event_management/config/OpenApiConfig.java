package ro.proiect.event_management.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI myOpenAPI() {
        // 1. Configurarea Serverului (URL-ul de baza)
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8080");
        devServer.setDescription("Server de Dezvoltare Local");

        // 2. Configurarea Informatiilor de Contact (Optional)
        Contact contact = new Contact();
//        contact.setEmail("student@upb.ro");
//        contact.setName("Echipa Proiect");

        // 3. Configurarea Titlului si Descrierii Generale
        Info info = new Info()
                .title("Sistem de Gestiune Evenimente Universitare API")
                .version("1.0")
                .contact(contact)
                .description("Aceasta este documentația oficială pentru Backend-ul aplicației de proiect.\n\n" +
                        "Aici găsiți toate endpoint-urile necesare pentru:\n" +
                        "- **Autentificare** (Login/Register)\n" +
                        "- **Evenimente** (CRUD, Aprobare)\n" +
                        "- **Bilete** (Achiziție, QR Code)\n" +
                        "- **Social** (Recenzii, Favorite, Notificări)");

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer));
    }
}