package com.finantel.exportar_reportes.aplicacion;

import com.finantel.exportar_reportes.dominio.repository.ExportarReportesRepository;
import com.finantel.exportar_reportes.infraestructura.mapping.FactoryReporteCreador;
import com.finantel.exportar_reportes.infraestructura.persistence.ExportarReportesConfig;
import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CU12ExportarReportesLimitesTest {

    private final ExportarReportesConfig config = new ExportarReportesConfig();

    @Mock
    private ExportarReportesRepository repo;

    @InjectMocks
    private ExportarReportesService service;

    @Test
    void CP_L1_tipo_vacio_es_rechazado() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> FactoryReporteCreador.crearReporte(""));

        assertTrue(ex.getMessage().contains("Tipo no soportado"));
    }

    @Test
    void CP_L2_tipo_nulo_es_rechazado_con_mensaje_controlado() {
        // Comportamiento esperado: IllegalArgumentException.
        // Si lanza NullPointerException, es un defecto: validar null antes de toUpperCase().
        assertThrows(IllegalArgumentException.class,
            () -> FactoryReporteCreador.crearReporte(null));
    }

    @Test
    void CP_L3_formato_nulo_no_es_valido() {
        assertFalse(config.esFormatoValido(null));
    }

    @Test
    void CP_L4_exportarPDF_con_muchos_asientos_indica_la_cantidad_exacta() {
        when(repo.findAll()).thenReturn(Collections.nCopies(1000, new AsientoContable()));

        assertEquals("Reporte PDF generado con 1000 asientos contables", service.exportarPDF());
    }
}