package co.edu.ucc.insumap.modelo;

import co.edu.ucc.insumap.modelo.enums.EstadoRecordatorio;
import co.edu.ucc.insumap.modelo.enums.TipoInsulina;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

/** Notificacion recordatoria generada a partir de un cronograma de dosis (RF10). Coleccion "recordatorios". */
@Document(collection = "recordatorios")
@CompoundIndex(name = "cronograma_fecha_original", def = "{'cronogramaId': 1, 'fechaOriginal': 1}", unique = true)
public class Recordatorio {

    @Id
    private String id;
    private String pacienteId;
    private String cronogramaId;
    private TipoInsulina tipoInsulina;
    private double unidades;
    /** Fecha en la que el cronograma programo la dosis (no cambia al aplazar). */
    private Instant fechaOriginal;
    /** Fecha en la que se debe despachar la notificacion (cambia al aplazar). */
    private Instant fechaProgramada;
    private Instant fechaEnvio;
    private Instant fechaRespuesta;
    private EstadoRecordatorio estado = EstadoRecordatorio.PENDIENTE;
    private int aplazamientos;
    private String aplicacionId;

    /** Un recordatorio abierto aun espera respuesta del paciente. */
    public boolean estaAbierto() {
        return estado == EstadoRecordatorio.PENDIENTE
                || estado == EstadoRecordatorio.ENVIADO
                || estado == EstadoRecordatorio.APLAZADO;
    }

    public boolean puedeAplazarse(int maximoAplazamientos) {
        return estaAbierto() && aplazamientos < maximoAplazamientos;
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

    public String getCronogramaId() {
        return cronogramaId;
    }

    public void setCronogramaId(String cronogramaId) {
        this.cronogramaId = cronogramaId;
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

    public Instant getFechaOriginal() {
        return fechaOriginal;
    }

    public void setFechaOriginal(Instant fechaOriginal) {
        this.fechaOriginal = fechaOriginal;
    }

    public Instant getFechaProgramada() {
        return fechaProgramada;
    }

    public void setFechaProgramada(Instant fechaProgramada) {
        this.fechaProgramada = fechaProgramada;
    }

    public Instant getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(Instant fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public Instant getFechaRespuesta() {
        return fechaRespuesta;
    }

    public void setFechaRespuesta(Instant fechaRespuesta) {
        this.fechaRespuesta = fechaRespuesta;
    }

    public EstadoRecordatorio getEstado() {
        return estado;
    }

    public void setEstado(EstadoRecordatorio estado) {
        this.estado = estado;
    }

    public int getAplazamientos() {
        return aplazamientos;
    }

    public void setAplazamientos(int aplazamientos) {
        this.aplazamientos = aplazamientos;
    }

    public String getAplicacionId() {
        return aplicacionId;
    }

    public void setAplicacionId(String aplicacionId) {
        this.aplicacionId = aplicacionId;
    }
}
