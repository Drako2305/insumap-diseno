package co.edu.ucc.insumap.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Beans transversales: reloj del sistema, planificador, CORS y documentacion OpenAPI. */
@Configuration
@EnableScheduling
public class ConfiguracionGeneral implements WebMvcConfigurer {

    /** Reloj inyectable: permite fijar la hora en las pruebas del motor y de los recordatorios. */
    @Bean
    public Clock reloj() {
        return Clock.systemUTC();
    }

    @Bean
    public OpenAPI documentacionApi() {
        return new OpenAPI().info(new Info()
                .title("Insumap API")
                .version("1.0.0")
                .description("Sistema inteligente de mapeo corporal y gestion de rotacion para la administracion "
                        + "de insulina. Proyecto academico de Diseno de Software (no es un dispositivo medico).")
                .contact(new Contact().name("Insumap - Diseno de Software")));
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**").allowedOriginPatterns("*").allowedMethods("GET", "POST", "PUT", "PATCH",
                "DELETE", "OPTIONS");
    }
}
