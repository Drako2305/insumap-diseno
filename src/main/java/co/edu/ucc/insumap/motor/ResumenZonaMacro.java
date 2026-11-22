package co.edu.ucc.insumap.motor;

import co.edu.ucc.insumap.modelo.enums.ZonaMacro;

/**
 * Uso de una zona macro dentro de la ventana de analisis (RF09).
 *
 * @param zona              zona macro
 * @param usosEnVentana     aplicaciones en los ultimos N dias (30 por defecto)
 * @param diasSinUso        dias desde la ultima aplicacion (-1 si nunca se ha usado)
 * @param saturada          su uso supera el promedio de las 4 zonas en la ventana
 * @param olvidada          no se usa hace 15 dias o mas
 * @param factorPrioridad   multiplicador aplicado al puntaje de sus microzonas
 */
public record ResumenZonaMacro(
        ZonaMacro zona,
        int usosEnVentana,
        double diasSinUso,
        boolean saturada,
        boolean olvidada,
        double factorPrioridad) {
}
