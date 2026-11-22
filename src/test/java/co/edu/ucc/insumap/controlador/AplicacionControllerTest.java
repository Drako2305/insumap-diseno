package co.edu.ucc.insumap.controlador;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.ucc.insumap.dto.aplicacion.AplicacionResponse;
import co.edu.ucc.insumap.dto.aplicacion.RegistroAplicacionResponse;
import co.edu.ucc.insumap.excepcion.ReglaNegocioException;
import co.edu.ucc.insumap.modelo.enums.Lado;
import co.edu.ucc.insumap.modelo.enums.RegistradoPor;
import co.edu.ucc.insumap.modelo.enums.Semaforo;
import co.edu.ucc.insumap.modelo.enums.TipoInsulina;
import co.edu.ucc.insumap.modelo.enums.ZonaMacro;
import co.edu.ucc.insumap.servicio.AplicacionService;
import co.edu.ucc.insumap.servicio.ReporteService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AplicacionController.class)
class AplicacionControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AplicacionService aplicacionService;

    @MockitoBean
    private ReporteService reporteService;

    @Test
    void registrarDevuelve201ConAdvertenciaNoBloqueante() throws Exception {
        AplicacionResponse aplicacion = new AplicacionResponse("a1", "p1", "abd_izq_04", ZonaMacro.ABDOMEN,
                Lado.IZQUIERDO, 0, 3, TipoInsulina.RAPIDA, 6, Instant.parse("2026-11-22T12:00:00Z"),
                RegistradoPor.PACIENTE, null, Semaforo.ROJO, false, false, null, null);
        when(aplicacionService.registrar(eq("p1"), any()))
                .thenReturn(new RegistroAplicacionResponse(aplicacion, "Microzona bloqueada", List.of("abd_izq_04"), 1));

        mvc.perform(post("/api/pacientes/p1/aplicaciones").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"microzonaId\":\"abd_izq_04\",\"tipoInsulina\":\"RAPIDA\",\"unidades\":6}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.aplicacion.semaforoAlRegistrar").value("ROJO"))
                .andExpect(jsonPath("$.advertencia").value("Microzona bloqueada"));
    }

    @Test
    void registrarValidaElCuerpo() throws Exception {
        mvc.perform(post("/api/pacientes/p1/aplicaciones").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"microzonaId\":\"\",\"unidades\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles").isArray());
    }

    @Test
    void deshacerSinRegistrosDevuelve409() throws Exception {
        when(aplicacionService.deshacerUltima("p1"))
                .thenThrow(new ReglaNegocioException("No hay registros de inyeccion para deshacer"));

        mvc.perform(post("/api/pacientes/p1/aplicaciones/deshacer"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("No hay registros de inyeccion para deshacer"));
    }

    @Test
    void zonaInvalidaEnFiltroDevuelve400() throws Exception {
        mvc.perform(get("/api/pacientes/p1/aplicaciones").param("zona", "CABEZA"))
                .andExpect(status().isBadRequest());
    }
}
