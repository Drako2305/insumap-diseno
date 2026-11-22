package co.edu.ucc.insumap.repositorio;

import co.edu.ucc.insumap.modelo.CronogramaDosis;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CronogramaRepository extends MongoRepository<CronogramaDosis, String> {

    List<CronogramaDosis> findByPacienteId(String pacienteId);

    List<CronogramaDosis> findByActivoTrue();

    void deleteByPacienteId(String pacienteId);
}
