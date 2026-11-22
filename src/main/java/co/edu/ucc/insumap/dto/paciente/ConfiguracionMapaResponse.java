package co.edu.ucc.insumap.dto.paciente;

import co.edu.ucc.insumap.modelo.ConfiguracionMapa;

public record ConfiguracionMapaResponse(int filas, int columnas, int microzonasPorLado, int totalMicrozonas) {

    public static ConfiguracionMapaResponse desde(ConfiguracionMapa c) {
        return new ConfiguracionMapaResponse(c.getFilas(), c.getColumnas(), c.getFilas() * c.getColumnas(),
                c.totalMicrozonas());
    }
}
