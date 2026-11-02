package com.finantel.exportar_reportes.aplicacion;

import com.finantel.exportar_reportes.dominio.repository.ExportarReportesRepository;
import com.finantel.exportar_reportes.infraestructura.mapping.FactoryReporte;
import com.finantel.exportar_reportes.infraestructura.mapping.FactoryReporteCreador;
import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CU12ExportarReportesUnitTest {

    @Mock
    private ExportarReportesRepository repo;

    @InjectMocks
    private ExportarReportesService service;

    @Test
    void CP_U1_exportarPDF_indica_la_cantidad_de_asientos() {
        when(repo.findAll()).thenReturn(List.of(new AsientoContable(), new AsientoContable()));

        assertEquals("Reporte PDF generado con 2 asientos contables", service.exportarPDF());
    }

    @Test
    void CP_U2_exportarExcel_sin_asientos_indica_cero() {
        when(repo.findAll()).thenReturn(List.of());

        assertEquals("Reporte Excel generado con 0 asientos contables", service.exportarExcel());
    }

    @Test
    void CP_U3_la_fabrica_PDF_declara_su_tipo_y_patron() {
        FactoryReporte reporte = FactoryReporteCreador.crearReporte("PDF");

        assertEquals("PDF", reporte.getTipo());
        assertTrue(reporte.getPatron().contains("FactoryReportePDF"));
    }
}