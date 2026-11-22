package co.edu.ucc.insumap.modelo;

import co.edu.ucc.insumap.modelo.enums.Lado;
import co.edu.ucc.insumap.modelo.enums.RegistradoPor;
import co.edu.ucc.insumap.modelo.enums.Semaforo;
import co.edu.ucc.insumap.modelo.enums.TipoInsulina;
import co.edu.ucc.insumap.modelo.enums.ZonaMacro;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

/** Registro de una inyeccion de insulina en una microzona (RF03, RF11). Coleccion "aplicaciones". */
@Document(collection = "aplicaciones")
@CompoundIndex(name = "paciente_fecha", def = "{'pacienteId': 1, 'fechaHora': -1}")
public class Aplicacion {

    @Id
    private String id;
    private String pacienteId;
    private String microzonaId;
    private ZonaMacro zonaMacro;
    private Lado lado;
    private int fila;
    private int columna;
    private TipoInsulina tipoInsulina;
    private double unidades;
    private Instant fechaHora;
    private RegistradoPor registradoPor;
    private String notas;
    /** Estado del semaforo de la microzona al momento de registrar (permite auditar advertencias, RF04). */
    private Semaforo semaforoAlRegistrar;
    /** Indica si se aplico en la microzona sugerida por el motor (adherencia a la rotacion). */
    private boolean enZonaRecomendada;
    /** Marcado por "Deshacer" (RF05): la aplicacion deja de contar para el estado del mapa. */
    private boolean deshecha;
    private Instant fechaDeshecha;
    private String recordatorioId;

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

    public String getMicrozonaId() {
        return microzonaId;
    }

    public void setMicrozonaId(String microzonaId) {
        this.microzonaId = microzonaId;
    }

    public ZonaMacro getZonaMacro() {
        return zonaMacro;
    }

    public void setZonaMacro(ZonaMacro zonaMacro) {
        this.zonaMacro = zonaMacro;
    }

    public Lado getLado() {
        return lado;
    }

    public void setLado(Lado lado) {
        this.lado = lado;
    }

    public int getFila() {
        return fila;
    }

    public void setFila(int fila) {
        this.fila = fila;
    }

    public int getColumna() {
        return columna;
    }

    public void setColumna(int columna) {
        this.columna = columna;
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

    public Instant getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(Instant fechaHora) {
        this.fechaHora = fechaHora;
    }

    public RegistradoPor getRegistradoPor() {
        return registradoPor;
    }

    public void setRegistradoPor(RegistradoPor registradoPor) {
        this.registradoPor = registradoPor;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public Semaforo getSemaforoAlRegistrar() {
        return semaforoAlRegistrar;
    }

    public void setSemaforoAlRegistrar(Semaforo semaforoAlRegistrar) {
        this.semaforoAlRegistrar = semaforoAlRegistrar;
    }

    public boolean isEnZonaRecomendada() {
        return enZonaRecomendada;
    }

    public void setEnZonaRecomendada(boolean enZonaRecomendada) {
        this.enZonaRecomendada = enZonaRecomendada;
    }

    public boolean isDeshecha() {
        return deshecha;
    }

    public void setDeshecha(boolean deshecha) {
        this.deshecha = deshecha;
    }

    public Instant getFechaDeshecha() {
        return fechaDeshecha;
    }

    public void setFechaDeshecha(Instant fechaDeshecha) {
        this.fechaDeshecha = fechaDeshecha;
    }

    public String getRecordatorioId() {
        return recordatorioId;
    }

    public void setRecordatorioId(String recordatorioId) {
        this.recordatorioId = recordatorioId;
    }
}
