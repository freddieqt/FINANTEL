package com.finantel.detectar_inconsistencias.aplicacion;

import com.finantel.detectar_inconsistencias.dominio.repository.InconsistenciaRepository;
import com.finantel.detectar_inconsistencias.infraestructura.persistence.InconsistenciaConfig;
import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CU09DetectarInconsistenciasLimitesTest {

    private final InconsistenciaConfig config = new InconsistenciaConfig();

    @Mock
    private InconsistenciaRepository repo;

    @InjectMocks
    private DetectarInconsistenciasService service;

    private AsientoContable asiento(double debe, double haber) {
        AsientoContable a = new AsientoContable();
        a.setDebe(debe);
        a.setHaber(haber);
        return a;
    }

    @Test
    void CP_L1_diferencia_de_exactamente_0_01_se_considera_balanceada() {
        // Comportamiento esperado: la tolerancia 0.01 es inclusiva.
        // Si falla, es un defecto de precisión de punto flotante: usar BigDecimal como en CU01.
        assertTrue(config.estaBalanceado(100.0, 100.01));
    }

    @Test
    void CP_L2_diferencia_de_0_02_se_considera_descuadrada() {
        assertFalse(config.estaBalanceado(100.0, 100.02));
    }

    @Test
    void CP_L3_servicio_acepta_diferencia_de_0_01() {
        when(repo.findById(3)).thenReturn(Optional.of(asiento(100.0, 100.01)));

        assertEquals("Sin inconsistencias detectadas", service.detectarErrores(3));
    }

    @Test
    void CP_L4_asiento_inexistente_lanza_excepcion() {
        when(repo.findById(999)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.detectarErrores(999));
        assertEquals("Asiento no encontrado", ex.getMessage());
    }

    @Test
    void CP_L5_sin_asientos_detectarTodos_devuelve_lista_vacia() {
        when(repo.findAll()).thenReturn(List.of());

        assertTrue(service.detectarTodos().isEmpty());
    }
}