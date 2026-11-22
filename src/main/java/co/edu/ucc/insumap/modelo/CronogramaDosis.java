package co.edu.ucc.insumap.modelo;

import co.edu.ucc.insumap.modelo.enums.TipoInsulina;
import java.time.DayOfWeek;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/** Cronograma de dosis configurado por el paciente (RF10). Coleccion "cronogramas". */
@Document(collection = "cronogramas")
public class CronogramaDosis {

    @Id
    private String id;
    @Indexed
    private String pacienteId;
    private String nombre;
    private TipoInsulina tipoInsulina;
    private double unidades;
    /** Horas del dia en formato HH:mm (hora local del paciente). */
    private List<String> horas = new ArrayList<>();
    private Set<DayOfWeek> diasSemana = EnumSet.allOf(DayOfWeek.class);
    private boolean activo = true;
    private Instant fechaCreacion;

    /** Indica si el cronograma debe generar dosis en el dia indicado. */
    public boolean aplicaEnDia(DayOfWeek dia) {
        return activo && diasSemana.contains(dia);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(String pacienteId) {
        this.pacienteId = pacienteId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoInsulina getTipoInsulina() {
        return tipoInsulina;
    }

    public void setTipoInsulina(TipoInsulina tipoInsulina) {
        this.tipoInsulina = tipoInsulina;
    }

    public double getUnidades() {
        return unidades;
    }

    public void setUnidades(double unidades) {
        this.unidades = unidades;
    }

    public List<String> getHoras() {
        return horas;
    }

    public void setHoras(List<String> horas) {
        this.horas = horas;
    }

    public Set<DayOfWeek> getDiasSemana() {
        return diasSemana;
    }

    public void setDiasSemana(Set<DayOfWeek> diasSemana) {
        this.diasSemana = diasSemana;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Instant fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
