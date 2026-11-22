package co.edu.ucc.insumap.controlador;

import co.edu.ucc.insumap.config.ParametrosMotor;
import co.edu.ucc.insumap.dto.ZonaCatalogoResponse;
import co.edu.ucc.insumap.modelo.enums.ZonaMacro;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalogo")
@Tag(name = "6. Catalogo", description = "Zonas macro y tiempos base de recuperacion (RF01, RF08)")
public class CatalogoController {

    private final ParametrosMotor parametros;

    public CatalogoController(ParametrosMotor parametros) {
        this.parametros = parametros;
    }

    @GetMapping("/zonas")
    @Operation(summary = "Listar zonas macro")
    public List<ZonaCatalogoResponse> zonas() {
        return Arrays.stream(ZonaMacro.values())
                .map(z -> new ZonaCatalogoResponse(z, z.getPrefijo(), z.getNombre(), parametros.horasBase(z)))
                .toList();
    }
}
