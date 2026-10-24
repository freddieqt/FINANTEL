package com.finantel.visualizar_reportes_graficos.aplicacion;

import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import com.finantel.visualizar_reportes_graficos.dominio.repository.ReportesGraficosRepository;

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
class CU13VisualizarReportesGraficosEquivalenciaTest {

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
    void CP_E1_cada_tipo_valido_forma_su_propio_grupo() {
        when(repo.findAll()).thenReturn(List.of(
            asiento("Apertura", 1, 1),
            asiento("Operación ordinaria", 1, 1),
            asiento("Ajuste", 1, 1),
            asiento("Cierre", 1, 1),
            asiento("Reversión", 1, 1)
        ));

        Map<String, Long> grupos = service.agruparPorTipo();

        assertEquals(5, grupos.size());
        grupos.values().forEach(cantidad -> assertEquals(1L, cantidad));
    }

    @Test
    void CP_E2_asiento_sin_tipo_se_agrupa_como_sin_tipo() {
        when(repo.findAll()).thenReturn(List.of(asiento(null, 1, 1)));

        assertEquals(1L, service.agruparPorTipo().get("Sin tipo"));
    }

    @Test
    void CP_E3_resultado_positivo_cuando_haber_supera_a_debe() {
        when(repo.findAll()).thenReturn(List.of(asiento("Ingresos", 100.0, 150.0)));

        assertEquals(50.0, ((Number) service.generarResumen().get("resultado")).doubleValue());
    }
}