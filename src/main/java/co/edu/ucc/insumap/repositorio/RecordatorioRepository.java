package co.edu.ucc.insumap.repositorio;

import co.edu.ucc.insumap.modelo.Recordatorio;
import co.edu.ucc.insumap.modelo.enums.EstadoRecordatorio;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RecordatorioRepository extends MongoRepository<Recordatorio, String> {

    boolean existsByCronogramaIdAndFechaOriginal(String cronogramaId, Instant fechaOriginal);

    /** Recordatorios que ya deben despacharse, en orden cronologico (alimenta la cola FIFO). */
    List<Recordatorio> findByEstadoInAndFechaProgramadaLessThanEqualOrderByFechaProgramadaAsc(
            Collection<EstadoRecordatorio> estados, Instant hasta);

    List<Recordatorio> findByPacienteIdOrderByFechaProgramadaDesc(String pacienteId);

    List<Recordatorio> findByPacienteIdAndEstadoOrderByFechaProgramadaDesc(
            String pacienteId, EstadoRecordatorio estado);

    void deleteByPacienteId(String pacienteId);

    void deleteByCronogramaIdAndEstadoIn(String cronogramaId, Collection<EstadoRecordatorio> estados);
}
