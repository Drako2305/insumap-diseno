package co.edu.ucc.insumap.motor;

import co.edu.ucc.insumap.config.ParametrosMotor;
import co.edu.ucc.insumap.modelo.enums.Semaforo;
import co.edu.ucc.insumap.modelo.enums.ZonaMacro;
import org.springframework.stereotype.Component;

/**
 * Calcula el tiempo de recuperacion biologica (RF08) y el semaforo (RF06) de una microzona.
 *
 * <pre>
 * horasRequeridas = horasBase(zona) x min(factorMaximo, 1 + factorFrecuencia x max(0, usos - 1))
 * progreso        = horasTranscurridas / horasRequeridas
 * semaforo        = ROJO si progreso &lt; umbralRojo; AMARILLO si progreso &lt; 1; VERDE en otro caso
 * </pre>
 */
@Component
public class CalculadoraRecuperacion {

    private final ParametrosMotor parametros;

    public CalculadoraRecuperacion(ParametrosMotor parametros) {
        this.parametros = parametros;
    }

    public double horasRecuperacion(ZonaMacro zona, int usosEnVentana) {
        double multiplicador = 1 + parametros.factorFrecuencia() * Math.max(0, usosEnVentana - 1);
        return parametros.horasBase(zona) * Math.min(parametros.factorFrecuenciaMaximo(), multiplicador);
    }

    /** Horas que se bloquea una microzona vecina a una aplicacion (RF07). */
    public double horasBloqueoAdyacente(ZonaMacro zona, int usosEnVentana) {
        return horasRecuperacion(zona, usosEnVentana) * parametros.factorAdyacencia();
    }

    public double progreso(double horasTranscurridas, double horasRequeridas) {
        if (horasRequeridas <= 0) {
            return 1.0;
        }
        return Math.max(0.0, horasTranscurridas / horasRequeridas);
    }

    public Semaforo semaforo(double progreso) {
        if (progreso < parametros.umbralRojo()) {
            return Semaforo.ROJO;
        }
        return progreso < 1.0 ? Semaforo.AMARILLO : Semaforo.VERDE;
    }
}
