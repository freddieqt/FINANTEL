package com.finantel.visualizar_reportes_graficos.aplicacion;

import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import com.finantel.visualizar_reportes_graficos.dominio.entity.ReporteGrafico;
import com.finantel.visualizar_reportes_graficos.dominio.repository.ReportesGraficosRepository;
import com.finantel.visualizar_reportes_graficos.infraestructura.mapping.ReportesGraficosMapper;

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
class CU13VisualizarReportesGraficosUnitTest {

    @Mock
    private ReportesGraficosRepository repo;

    @InjectMocks
    private VisualizarReportesGraficosService service;

    private AsientoContable asiento(String tipo, double debe, double haber) {
        AsientoContable a = new AsientoContable();
        a.setTipo(tipo);
        a.setDebe(debe);
        a.setHaber(haber);
        return a;
    }

    @Test
    void CP_U1_resumen_suma_debe_y_haber_y_calcula_el_resultado() {
        when(repo.findAll()).thenReturn(List.of(
            asiento("Cierre", 100.0, 0.0),
            asiento("Cierre", 0.0, 100.0)
        ));

        Map<String, Object> r = service.generarResumen();

        assertEquals(2, ((Number) r.get("totalAsientos")).intValue());
        assertEquals(100.0, ((Number) r.get("totalDebe")).doubleValue());
        assertEquals(100.0, ((Number) r.get("totalHaber")).doubleValue());
        assertEquals(0.0, ((Number) r.get("resultado")).doubleValue());
    }

    @Test
    void CP_U2_agruparPorTipo_cuenta_los_asientos_de_cada_tipo() {
        when(repo.findAll()).thenReturn(List.of(
            asiento("Cierre", 1.0, 1.0),
            asiento("Cierre", 1.0, 1.0),
            asiento("Ajuste", 1.0, 1.0)
        ));

        Map<String, Long> grupos = service.agruparPorTipo();

        assertEquals(2L, grupos.get("Cierre"));
        assertEquals(1L, grupos.get("Ajuste"));
    }

    @Test
    void CP_U3_el_mapper_construye_el_grafico_con_sus_campos() {
        ReporteGrafico g = new ReportesGraficosMapper().toGrafico("Balance", 500.0, "Activos");

        assertEquals("Balance", g.getTipo());
        assertEquals(500.0, g.getValor());
        assertEquals("Activos", g.getEtiqueta());
    }
}