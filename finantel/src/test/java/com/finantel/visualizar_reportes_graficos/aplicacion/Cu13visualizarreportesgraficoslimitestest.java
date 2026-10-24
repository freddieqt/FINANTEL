package com.finantel.visualizar_reportes_graficos.aplicacion;

import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import com.finantel.visualizar_reportes_graficos.dominio.repository.ReportesGraficosRepository;
import com.finantel.visualizar_reportes_graficos.infraestructura.persistence.ReportesGraficosConfig;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CU13VisualizarReportesGraficosLimitesTest {

    private final ReportesGraficosConfig config = new ReportesGraficosConfig();

    @Mock
    private ReportesGraficosRepository repo;

    @InjectMocks
    private VisualizarReportesGraficosService service;

    @Test
    void CP_L1_sin_asientos_el_resumen_queda_en_cero() {
        when(repo.findAll()).thenReturn(List.of());

        Map<String, Object> r = service.generarResumen();

        assertEquals(0, ((Number) r.get("totalAsientos")).intValue());
        assertEquals(0.0, ((Number) r.get("resultado")).doubleValue());
    }

    @Test
    void CP_L2_debe_y_haber_nulos_se_toman_como_cero() {
        AsientoContable a = new AsientoContable();
        a.setTipo("Cierre");
        when(repo.findAll()).thenReturn(List.of(a));

        Map<String, Object> r = service.generarResumen();

        assertEquals(0.0, ((Number) r.get("totalDebe")).doubleValue());
        assertEquals(0.0, ((Number) r.get("totalHaber")).doubleValue());
    }

    @Test
    void CP_L3_resultado_negativo_cuando_debe_supera_a_haber() {
        AsientoContable a = new AsientoContable();
        a.setTipo("Gastos");
        a.setDebe(200.0);
        a.setHaber(50.0);
        when(repo.findAll()).thenReturn(List.of(a));

        assertEquals(-150.0, ((Number) service.generarResumen().get("resultado")).doubleValue());
    }

    @Test
    void CP_L4_hayDatos_con_cero_registros_es_falso() {
        assertFalse(config.hayDatos(0));
    }

    @Test
    void CP_L5_hayDatos_con_un_registro_es_verdadero() {
        assertTrue(config.hayDatos(1));
    }
}