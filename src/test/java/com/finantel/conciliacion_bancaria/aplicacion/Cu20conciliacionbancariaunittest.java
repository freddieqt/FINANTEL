package com.finantel.conciliacion_bancaria.aplicacion;

import com.finantel.conciliacion_bancaria.dominio.entity.MovimientoBancario;
import com.finantel.conciliacion_bancaria.dominio.repository.ConciliacionBancariaRepository;
import com.finantel.registrar_accion.aplicacion.AuditoriaService;
import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import com.finantel.registrar_asiento_contable.dominio.repository.AsientoContableRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CU20ConciliacionBancariaUnitTest {

    @Mock
    private ConciliacionBancariaRepository repoMovimientos;

    @Mock
    private AsientoContableRepository repoAsientos;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private ConciliacionBancariaService service;

    private AsientoContable asiento(double debe) {
        AsientoContable a = new AsientoContable();
        a.setDebe(debe);
        return a;
    }

    private MovimientoBancario movimiento(double monto) {
        MovimientoBancario m = new MovimientoBancario();
        m.setMonto(monto);
        return m;
    }

    @Test
    void CP_U1_saldos_iguales_quedan_conciliados() {
        when(repoAsientos.findAll()).thenReturn(List.of(asiento(100.0)));
        when(repoMovimientos.findAll()).thenReturn(List.of(movimiento(100.0)));

        Map<String, Object> r = service.conciliar("extracto.csv");

        assertEquals("Conciliado", r.get("estado"));
        assertEquals(0.0, ((Number) r.get("diferencia")).doubleValue(), 0.0001);
    }

    @Test
    void CP_U2_saldos_distintos_muestran_la_diferencia() {
        when(repoAsientos.findAll()).thenReturn(List.of(asiento(100.0)));
        when(repoMovimientos.findAll()).thenReturn(List.of(movimiento(80.0)));

        Map<String, Object> r = service.conciliar("extracto.csv");

        assertEquals("Con diferencias", r.get("estado"));
        assertEquals(-20.0, ((Number) r.get("diferencia")).doubleValue(), 0.0001);
    }

    @Test
    void CP_U3_la_conciliacion_se_registra_en_auditoria() {
        when(repoAsientos.findAll()).thenReturn(List.of(asiento(100.0)));
        when(repoMovimientos.findAll()).thenReturn(List.of(movimiento(100.0)));

        service.conciliar("extracto.csv");

        verify(auditoriaService).registrar(
            eq("sistema"), anyString(), eq("MovimientosBancarios"), isNull(), eq("Conciliado"));
    }

    @Test
    void CP_U4_listar_movimientos_devuelve_los_del_repositorio() {
        when(repoMovimientos.findAll()).thenReturn(List.of(movimiento(5.0)));

        assertEquals(1, service.listarMovimientos().size());
    }
}