package co.edu.ucc.insumap.modelo;

/** Cuidador o familiar que asiste al paciente en el registro de inyecciones. Embebido en Paciente. */
public class Cuidador {

    private String nombre;
    private String parentesco;
    private String telefono;
    private String email;

    public Cuidador() {
    }

    public Cuidador(String nombre, String parentesco, String telefono, String email) {
        this.nombre = nombre;
        this.parentesco = parentesco;
        this.telefono = telefono;
        this.email = email;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getParentesco() {
        return parentesco;
    }

    public void setParentesco(String parentesco) {
        this.parentesco = parentesco;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
