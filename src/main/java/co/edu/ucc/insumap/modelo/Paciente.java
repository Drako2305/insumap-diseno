package co.edu.ucc.insumap.modelo;

import co.edu.ucc.insumap.modelo.enums.TipoDiabetes;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/** Paciente insulinodependiente. Coleccion "pacientes". */
@Document(collection = "pacientes")
public class Paciente {

    @Id
    private String id;
    private String nombres;
    private String apellidos;
    @Indexed(unique = true)
    private String documentoIdentidad;
    private LocalDate fechaNacimiento;
    private TipoDiabetes tipoDiabetes;
    private String email;
    private String telefono;
    private ConfiguracionMapa configuracionMapa;
    private List<Cuidador> cuidadores = new ArrayList<>();
    private MedicoTratante medicoTratante;
    /** Pila (LIFO) persistida con los ids de las ultimas aplicaciones, usada por "Deshacer" (RF05). */
    private List<String> pilaDeshacer = new ArrayList<>();
    private Instant fechaRegistro;

    public String nombreCompleto() {
        return nombres + " " + apellidos;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getDocumentoIdentidad() {
        return documentoIdentidad;
    }

    public void setDocumentoIdentidad(String documentoIdentidad) {
        this.documentoIdentidad = documentoIdentidad;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public TipoDiabetes getTipoDiabetes() {
        return tipoDiabetes;
    }

    public void setTipoDiabetes(TipoDiabetes tipoDiabetes) {
        this.tipoDiabetes = tipoDiabetes;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public ConfiguracionMapa getConfiguracionMapa() {
        return configuracionMapa;
    }

    public void setConfiguracionMapa(ConfiguracionMapa configuracionMapa) {
        this.configuracionMapa = configuracionMapa;
    }

    public List<Cuidador> getCuidadores() {
        return cuidadores;
    }

    public void setCuidadores(List<Cuidador> cuidadores) {
        this.cuidadores = cuidadores == null ? new ArrayList<>() : cuidadores;
    }

    public MedicoTratante getMedicoTratante() {
        return medicoTratante;
    }

    public void setMedicoTratante(MedicoTratante medicoTratante) {
        this.medicoTratante = medicoTratante;
    }

    public List<String> getPilaDeshacer() {
        return pilaDeshacer;
    }

    public void setPilaDeshacer(List<String> pilaDeshacer) {
        this.pilaDeshacer = pilaDeshacer == null ? new ArrayList<>() : pilaDeshacer;
    }

    public Instant getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Instant fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
