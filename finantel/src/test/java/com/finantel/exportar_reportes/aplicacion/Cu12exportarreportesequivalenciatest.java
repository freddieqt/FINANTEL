package com.finantel.exportar_reportes.aplicacion;

import com.finantel.exportar_reportes.infraestructura.mapping.FactoryReporteCreador;
import com.finantel.exportar_reportes.infraestructura.persistence.ExportarReportesConfig;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class CU12ExportarReportesEquivalenciaTest {

    private final ExportarReportesConfig config = new ExportarReportesConfig();

    @ParameterizedTest
    @CsvSource({
        "PDF, PDF",
        "pdf, PDF",
        "Excel, EXCEL",
        "EXCEL, EXCEL"
    })
    void CP_E1_formatos_soportados_crean_el_reporte_correcto(String entrada, String tipoEsperado) {
        assertEquals(tipoEsperado, FactoryReporteCreador.crearReporte(entrada).getTipo());
    }

    @ParameterizedTest
    @ValueSource(strings = {"PDF", "pdf", "Excel", "EXCEL"})
    void CP_E2_formatos_validos_son_aceptados(String formato) {
        assertTrue(config.esFormatoValido(formato));
    }

    @Test
    void CP_E3_formato_no_soportado_es_rechazado() {
        assertFalse(config.esFormatoValido("Word"));
    }
}