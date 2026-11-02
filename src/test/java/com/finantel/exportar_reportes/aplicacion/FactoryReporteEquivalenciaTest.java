package com.finantel.exportar_reportes.aplicacion;

import com.finantel.exportar_reportes.infraestructura.mapping.FactoryReporteCreador;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FactoryReporteEquivalenciaTest {

    // ENTRADA: formato = "PDF"
    // RESULTADO: Se crea el generador de reporte PDF
    @Test
    void EQ_CU12_formato_PDF_se_acepta() {
        assertNotNull(FactoryReporteCreador.crearReporte("PDF"));
    }

    // ENTRADA: formato = "EXCEL"
    // RESULTADO: Se crea el generador de reporte Excel
    @Test
    void EQ_CU12_formato_EXCEL_se_acepta() {
        assertNotNull(FactoryReporteCreador.crearReporte("EXCEL"));
    }

    // ENTRADA: formato = "pdf" (minúscula)
    // RESULTADO: Se crea el generador de reporte PDF (el código compara en mayúsculas)
    @Test
    void EQ_CU12_formato_pdf_en_minuscula_se_acepta() {
        assertNotNull(FactoryReporteCreador.crearReporte("pdf"));
    }

    // ENTRADA: formato = null
    // RESULTADO: Se rechaza con el mensaje "Tipo no soportado"
    @Test
    void EQ_CU12_formato_nulo_se_rechaza() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> FactoryReporteCreador.crearReporte(null));
        assertTrue(ex.getMessage().contains("Tipo no soportado"));
    }
}