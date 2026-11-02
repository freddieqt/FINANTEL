package com.finantel.generar_estados_financieros.aplicacion;

import com.finantel.generar_estados_financieros.dominio.repository.EstadosFinancierosRepository;
import com.finantel.generar_estados_financieros.infraestructura.persistence.EstadosFinancierosConfig;

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
class CU11GenerarEstadosLimitesTest {

    private final EstadosFinancierosConfig config = new EstadosFinancierosConfig();

    @Mock
    private EstadosFinancierosRepository repo;

    @InjectMocks
    private GenerarEstadosFinancierosService service;

    private double valor(Map<String, Object> mapa, String clave) {
        return ((Number) mapa.get(clave)).doubleValue();
    }

    @Test
    void CP_L1_codigo_nulo_o_vacio_es_ignorado() {
        when(repo.findTodosSaldos()).thenReturn(List.<Object[]>of(
            new Object[]{null, "Sin código", "Ingresos", 500.0},
            new Object[]{"", "Vacío", "Ingresos", 500.0}
        ));

        assertEquals(0.0, valor(service.getBalanceGeneral(), "totalIngresos"));
    }

    @Test
    void CP_L2_cuenta_fuera_de_las_secciones_no_afecta_el_resultado() {
        when(repo.findTodosSaldos()).thenReturn(List.<Object[]>of(
            new Object[]{"1011", "Caja", "Activo Corriente", 900.0}
        ));

        assertEquals(0.0, valor(service.getBalanceGeneral(), "utilidadNeta"));
    }

    @Test
    void CP_L3_saldo_nulo_se_toma_como_cero() {
        when(repo.findTodosSaldos()).thenReturn(List.<Object[]>of(
            new Object[]{"5001", "Ventas", "Ingresos", null}
        ));

        assertEquals(0.0, valor(service.getBalanceGeneral(), "totalIngresos"));
    }

    @Test
    void CP_L4_saldo_negativo_se_muestra_en_valor_absoluto() {
        when(repo.findTodosSaldos()).thenReturn(List.<Object[]>of(
            new Object[]{"5001", "Ventas", "Ingresos", -500.0}
        ));

        assertEquals(500.0, valor(service.getBalanceGeneral(), "totalIngresos"));
    }

    @Test
    void CP_L5_sin_asientos_el_balance_real_queda_en_cero() {
        when(repo.findTodosSaldos()).thenReturn(List.<Object[]>of());

        Map<String, Object> r = service.getBalanceGeneralReal();

        assertEquals(0.0, valor(r, "totalActivo"));
        assertTrue(((List<?>) r.get("activos")).isEmpty());
    }

    @Test
    void CP_L6_sin_datos_hayDatos_es_falso() {
        assertFalse(config.hayDatos(0));
    }
}