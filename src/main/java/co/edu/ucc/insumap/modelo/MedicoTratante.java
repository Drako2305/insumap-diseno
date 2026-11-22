package co.edu.ucc.insumap.modelo;

/** Medico tratante / endocrinologo que consume los reportes exportados. Embebido en Paciente. */
public class MedicoTratante {

    private String nombre;
    private String especialidad;
    private String email;
    private String telefono;

    public MedicoTratante() {
    }

    public MedicoTratante(String nombre, String especialidad, String email, String telefono) {
        this.nombre = nombre;
        this.especialidad = especialidad;
        this.email = email;
        this.telefono = telefono;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
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
}
