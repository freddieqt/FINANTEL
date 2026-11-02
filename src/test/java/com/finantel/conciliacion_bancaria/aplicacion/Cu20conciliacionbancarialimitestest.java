package com.finantel.conciliacion_bancaria.aplicacion;

import com.finantel.conciliacion_bancaria.infraestructura.persistence.ConciliacionBancariaConfig;
import com.finantel.conciliacion_bancaria.dominio.repository.ConciliacionBancariaRepository;
import com.finantel.registrar_accion.aplicacion.AuditoriaService;
import com.finantel.registrar_asiento_contable.dominio.repository.AsientoContableRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CU20ConciliacionBancariaLimitesTest {

    private final ConciliacionBancariaConfig config = new ConciliacionBancariaConfig();

    @Mock
    private ConciliacionBancariaRepository repoMovimientos;

    @Mock
    private AsientoContableRepository repoAsientos;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private ConciliacionBancariaService service;

    @Test
    void CP_L1_diferencia_de_exactamente_0_01_no_esta_conciliada() {
        assertFalse(config.estaConciliado(0.01));
    }

    @Test
    void CP_L2_diferencia_de_0_009_esta_conciliada() {
        assertTrue(config.estaConciliado(0.009));
    }

    @Test
    void CP_L3_diferencia_nula_no_debe_lanzar_error() {
        assertFalse(config.estaConciliado(null));
    }

    @Test
    void CP_L4_archivo_sin_nombre_no_debe_fallar() {
        assertDoesNotThrow(() -> service.conciliar(null));
    }

    @Test
    void CP_L5_sin_asientos_ni_movimientos_queda_conciliado() {
        assertEquals("Conciliado", service.conciliar("vacio.csv").get("estado"));
    }
}