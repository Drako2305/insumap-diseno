package co.edu.ucc.insumap.repositorio;

import co.edu.ucc.insumap.modelo.Aplicacion;
import java.time.Instant;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AplicacionRepository extends MongoRepository<Aplicacion, String> {

    /** Historial activo (sin deshechas) desde una fecha, usado por el motor de recomendacion. */
    List<Aplicacion> findByPacienteIdAndDeshechaFalseAndFechaHoraAfterOrderByFechaHoraDesc(
            String pacienteId, Instant desde);

    /** Historial activo en un rango, usado por la consulta y la exportacion PDF (RF11). */
    List<Aplicacion> findByPacienteIdAndDeshechaFalseAndFechaHoraBetweenOrderByFechaHoraDesc(
            String pacienteId, Instant desde, Instant hasta);

    long countByPacienteIdAndDeshechaFalse(String pacienteId);

    void deleteByPacienteId(String pacienteId);
}
