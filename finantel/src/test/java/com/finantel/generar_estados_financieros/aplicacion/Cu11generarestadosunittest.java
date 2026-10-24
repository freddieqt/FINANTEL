package com.finantel.generar_estados_financieros.aplicacion;

import com.finantel.generar_estados_financieros.dominio.repository.EstadosFinancierosRepository;

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
class CU11GenerarEstadosUnitTest {

    @Mock
    private EstadosFinancierosRepository repo;

    @InjectMocks
    private GenerarEstadosFinancierosService service;

    private double valor(Map<String, Object> mapa, String clave) {
        return ((Number) mapa.get(clave)).doubleValue();
    }

    @Test
    void CP_U1_utilidad_bruta_es_ingresos_menos_costos() {
        when(repo.findTodosSaldos()).thenReturn(List.of(
            new Object[]{"5001", "Ventas", "Ingresos", 1000.0},
            new Object[]{"6001", "Costo de ventas", "Gastos", 400.0}
        ));

        assertEquals(600.0, valor(service.getBalanceGeneral(), "utilidadBruta"));
    }

    @Test
    void CP_U2_utilidad_neta_descuenta_gastos_operativos_y_financieros() {
        when(repo.findTodosSaldos()).thenReturn(List.of(
            new Object[]{"5001", "Ventas", "Ingresos", 1000.0},
            new Object[]{"600", "Costo", "Gastos", 400.0},
            new Object[]{"6010", "Sueldos", "Gastos", 200.0},
            new Object[]{"6501", "Intereses", "Gastos", 100.0}
        ));

        assertEquals(300.0, valor(service.getBalanceGeneral(), "utilidadNeta"));
    }

    @Test
    void CP_U3_balance_real_agrupa_activo_pasivo_y_patrimonio() {
        when(repo.findTodosSaldos()).thenReturn(List.of(
            new Object[]{"1011", "Caja", "Activo Corriente", 1000.0},
            new Object[]{"2011", "Proveedores", "Pasivo Corriente", 300.0},
            new Object[]{"3011", "Capital", "Patrimonio", 700.0}
        ));

        Map<String, Object> r = service.getBalanceGeneralReal();

        assertEquals(1000.0, valor(r, "totalActivo"));
        assertEquals(300.0, valor(r, "totalPasivo"));
        assertEquals(700.0, valor(r, "totalPatrimonio"));
    }
}