package co.edu.ucc.insumap.repositorio;

import co.edu.ucc.insumap.modelo.Paciente;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PacienteRepository extends MongoRepository<Paciente, String> {

    Optional<Paciente> findByDocumentoIdentidad(String documentoIdentidad);

    boolean existsByDocumentoIdentidad(String documentoIdentidad);
}
