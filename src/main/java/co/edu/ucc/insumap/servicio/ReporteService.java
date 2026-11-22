package co.edu.ucc.insumap.servicio;

import co.edu.ucc.insumap.config.ParametrosRecordatorios;
import co.edu.ucc.insumap.dto.mapa.EstadisticasResponse;
import co.edu.ucc.insumap.modelo.Aplicacion;
import co.edu.ucc.insumap.modelo.Paciente;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

/** Genera el reporte PDF del historial de aplicaciones para el medico tratante (RF11). */
@Service
public class ReporteService {

    private static final float MARGEN = 50f;
    private static final float ALTO_LINEA = 15f;
    private static final float[] COLUMNAS = {50f, 150f, 230f, 310f, 385f, 440f, 500f};
    private static final String[] ENCABEZADOS = {"Fecha y hora", "Microzona", "Zona", "Insulina", "Unid.",
        "Registro", "Estado"};

    private final PacienteService pacienteService;
    private final AplicacionService aplicacionService;
    private final MapaCorporalService mapaCorporalService;
    private final ZoneId zona;
    private final DateTimeFormatter formatoFecha;
    private final Clock reloj;

    public ReporteService(PacienteService pacienteService, AplicacionService aplicacionService,
            MapaCorporalService mapaCorporalService, ParametrosRecordatorios parametros, Clock reloj) {
        this.pacienteService = pacienteService;
        this.aplicacionService = aplicacionService;
        this.mapaCorporalService = mapaCorporalService;
        this.zona = ZoneId.of(parametros.zonaHoraria());
        this.formatoFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(zona);
        this.reloj = reloj;
    }

    public byte[] generarHistorialPdf(String pacienteId, Instant desde, Instant hasta) {
        Paciente paciente = pacienteService.obtener(pacienteId);
        List<Aplicacion> aplicaciones = aplicacionService.consultarHistorial(pacienteId, desde, hasta, null);
        EstadisticasResponse estadisticas = mapaCorporalService.obtenerEstadisticas(pacienteId);

        try (PDDocument documento = new PDDocument(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Escritor escritor = new Escritor(documento);
            escritor.texto("Insumap - Historial de aplicaciones de insulina", 16, true);
            escritor.texto("Generado: " + formatoFecha.format(Instant.now(reloj)) + " (" + zona + ")", 9, false);
            escritor.espacio();
            escritor.texto("Paciente: " + paciente.nombreCompleto(), 11, true);
            escritor.texto("Documento: " + paciente.getDocumentoIdentidad() + "   Diabetes: "
                    + paciente.getTipoDiabetes() + "   Nacimiento: " + paciente.getFechaNacimiento(), 10, false);
            if (paciente.getMedicoTratante() != null) {
                escritor.texto("Medico tratante: " + paciente.getMedicoTratante().getNombre() + " ("
                        + valor(paciente.getMedicoTratante().getEspecialidad()) + ")", 10, false);
            }
            escritor.texto("Periodo: " + (desde == null ? "inicio" : formatoFecha.format(desde)) + " a "
                    + (hasta == null ? "hoy" : formatoFecha.format(hasta)), 10, false);
            escritor.espacio();

            escritor.texto("Resumen de rotacion (ultimos " + estadisticas.diasVentana() + " dias)", 12, true);
            escritor.texto(String.format(Locale.ROOT,
                    "Aplicaciones: %d   Adherencia a la zona sugerida: %.1f%%   Registros con advertencia: %d",
                    estadisticas.aplicacionesEnVentana(), estadisticas.porcentajeAdherencia(),
                    estadisticas.aplicacionesConAdvertencia()), 10, false);
            for (EstadisticasResponse.UsoZonaResponse uso : estadisticas.zonas()) {
                String estado = uso.saturada() ? "SATURADA" : uso.olvidada() ? "OLVIDADA" : "equilibrada";
                escritor.texto(String.format(Locale.ROOT, "  - %-8s %3d usos (%5.1f%%)  %s", uso.zona(), uso.usos(),
                        uso.porcentaje(), estado), 10, false);
            }
            escritor.espacio();

            escritor.texto("Detalle (" + aplicaciones.size() + " registros, del mas reciente al mas antiguo)", 12, true);
            escritor.fila(ENCABEZADOS, true);
            for (Aplicacion a : aplicaciones) {
                escritor.fila(new String[] {formatoFecha.format(a.getFechaHora()), a.getMicrozonaId(),
                    a.getZonaMacro().getNombre(), String.valueOf(a.getTipoInsulina()),
                    String.format(Locale.ROOT, "%.1f", a.getUnidades()), String.valueOf(a.getRegistradoPor()),
                    String.valueOf(a.getSemaforoAlRegistrar())}, false);
            }
            if (aplicaciones.isEmpty()) {
                escritor.texto("Sin aplicaciones registradas en el periodo.", 10, false);
            }
            escritor.espacio();
            escritor.texto("Documento exportado por el paciente. Insumap es un proyecto academico y no reemplaza el "
                    + "criterio medico.", 8, false);
            escritor.cerrar();
            documento.save(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar el PDF", e);
        }
    }

    private static String valor(String texto) {
        return texto == null ? "-" : texto;
    }

    /** Ayudante que escribe lineas y filas paginando automaticamente. */
    private static final class Escritor {

        private final PDDocument documento;
        private final PDType1Font normal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        private final PDType1Font negrita = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        private PDPageContentStream contenido;
        private float y;

        Escritor(PDDocument documento) throws IOException {
            this.documento = documento;
            nuevaPagina();
        }

        private void nuevaPagina() throws IOException {
            if (contenido != null) {
                contenido.close();
            }
            PDPage pagina = new PDPage(PDRectangle.A4);
            documento.addPage(pagina);
            contenido = new PDPageContentStream(documento, pagina);
            y = pagina.getMediaBox().getHeight() - MARGEN;
        }

        private void asegurarEspacio() throws IOException {
            if (y < MARGEN + ALTO_LINEA) {
                nuevaPagina();
            }
        }

        void texto(String linea, float tamano, boolean esNegrita) throws IOException {
            asegurarEspacio();
            escribir(linea, MARGEN, tamano, esNegrita);
            y -= ALTO_LINEA + (tamano > 11 ? 4 : 0);
        }

        void fila(String[] celdas, boolean esEncabezado) throws IOException {
            asegurarEspacio();
            for (int i = 0; i < celdas.length; i++) {
                escribir(celdas[i], COLUMNAS[i], 9, esEncabezado);
            }
            y -= ALTO_LINEA;
        }

        void espacio() {
            y -= ALTO_LINEA / 2;
        }

        private void escribir(String texto, float x, float tamano, boolean esNegrita) throws IOException {
            contenido.beginText();
            contenido.setFont(esNegrita ? negrita : normal, tamano);
            contenido.newLineAtOffset(x, y);
            contenido.showText(limpiar(texto));
            contenido.endText();
        }

        /** Las fuentes estandar usan WinAnsi: se reemplazan caracteres fuera de Latin-1. */
        private static String limpiar(String texto) {
            if (texto == null) {
                return "";
            }
            StringBuilder limpio = new StringBuilder(texto.length());
            for (char c : texto.toCharArray()) {
                limpio.append(c >= 32 && c <= 255 ? c : '?');
            }
            return limpio.toString();
        }

        void cerrar() throws IOException {
            contenido.close();
        }
    }
}
