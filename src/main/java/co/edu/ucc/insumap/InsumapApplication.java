package co.edu.ucc.insumap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/** Punto de entrada del backend de Insumap. */
@SpringBootApplication
@ConfigurationPropertiesScan
public class InsumapApplication {

    public static void main(String[] args) {
        SpringApplication.run(InsumapApplication.class, args);
    }
}
