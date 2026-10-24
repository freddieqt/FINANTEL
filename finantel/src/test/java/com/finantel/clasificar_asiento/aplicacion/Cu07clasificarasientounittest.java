package com.finantel.clasificar_asiento.aplicacion;

import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import com.finantel.registrar_asiento_contable.dominio.repository.AsientoContableRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CU07ClasificarAsientoUnitTest {

    @Mock
    private AsientoContableRepository repo;

    @InjectMocks
    private ClasificarAsientoService service;

    private AsientoContable asiento(String tipo) {
        AsientoContable a = new AsientoContable();
        a.setTipo(tipo);
        return a;
    }

    @Test
    void CP_U1_listarPorTipo_devuelve_solo_los_asientos_del_tipo_pedido() {
        when(repo.findAll()).thenReturn(List.of(asiento("Cierre"), asiento("Ajuste")));

        List<AsientoContable> resultado = service.listarPorTipo("Cierre");

        assertEquals(1, resultado.size());
        assertEquals("Cierre", resultado.get(0).getTipo());
    }

    @Test
    void CP_U2_listarPorTipo_ignora_mayusculas_y_minusculas() {
        when(repo.findAll()).thenReturn(List.of(asiento("Cierre")));

        List<AsientoContable> resultado = service.listarPorTipo("CIERRE");

        assertEquals(1, resultado.size());
    }

    @Test
    void CP_U3_getTiposDisponibles_devuelve_los_cinco_tipos_en_orden() {
        assertEquals(
            List.of("Apertura", "Operación ordinaria", "Ajuste", "Cierre", "Reversión"),
            service.getTiposDisponibles()
        );
    }
}