package co.edu.ucc.insumap.dto;

import co.edu.ucc.insumap.modelo.enums.ZonaMacro;

/** Zona macro con su prefijo y su tiempo base de recuperacion (RF01, RF08). */
public record ZonaCatalogoResponse(ZonaMacro zona, String prefijo, String nombre, double horasBaseRecuperacion) {
}
