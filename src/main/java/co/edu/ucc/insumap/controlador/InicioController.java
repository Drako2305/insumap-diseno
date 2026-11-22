package co.edu.ucc.insumap.controlador;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Redirige la raiz del servicio a la documentacion Swagger. */
@Controller
@Hidden
public class InicioController {

    @GetMapping("/")
    public String inicio() {
        return "redirect:/swagger-ui.html";
    }
}
