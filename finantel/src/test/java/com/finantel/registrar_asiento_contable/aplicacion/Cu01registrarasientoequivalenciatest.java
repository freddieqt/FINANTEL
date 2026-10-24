package com.finantel.registrar_asiento_contable.aplicacion;

import com.finantel.registrar_accion.aplicacion.AuditoriaService;
import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import com.finantel.registrar_asiento_contable.dominio.repository.AsientoContableRepository;
import com.finantel.validar_cuentas.dominio.entity.CuentaContable;
import com.finantel.validar_cuentas.dominio.repository.ValidarCuentasRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CU01RegistrarAsientoEquivalenciaTest {

    @Mock
    private AsientoContableRepository repo;

    @Mock
    private AuditoriaService auditoriaService;

    @Mock
    private ValidarCuentasRepository cuentaRepo;

    @InjectMocks
    private AsientoContableService service;

    private AsientoContable conTipo(String tipo) {
        AsientoContable a = new AsientoContable();
        a.setTipo(tipo);
        a.setDescripcion("Cobro cuota");
        a.setDebe(5000.0);
        a.setHaber(5000.0);
        a.setCuentaContable("1011");
        a.setMoneda("PEN");
        return a;
    }

    private void prepararCuentaActiva() {
        CuentaContable cuenta = new CuentaContable();
        cuenta.setEstado("Activa");
        lenient().when(cuentaRepo.findById("1011")).thenReturn(Optional.of(cuenta));
        lenient().when(repo.count()).thenReturn(0L);
        lenient().when(repo.existsByNumeroCorrelativo(anyString())).thenReturn(false);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Apertura", "Ajuste", "Cierre"})
    void CP_E1_tipos_validos_se_registran(String tipo) {
        prepararCuentaActiva();

        assertDoesNotThrow(() -> service.crear(conTipo(tipo)));
        verify(repo).save(any(AsientoContable.class));
    }

    @Test
    void CP_E2_tipo_no_valido_se_rechaza() {
        prepararCuentaActiva();

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.crear(conTipo("Gasto")));

        assertTrue(ex.getMessage().contains("Tipo"));
        verify(repo, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"PEN", "USD"})
    void CP_E3_monedas_validas_se_registran(String moneda) {
        prepararCuentaActiva();
        AsientoContable a = conTipo("Ajuste");
        a.setMoneda(moneda);

        assertDoesNotThrow(() -> service.crear(a));
    }

    @Test
    void CP_E4_moneda_no_valida_se_rechaza() {
        prepararCuentaActiva();
        AsientoContable a = conTipo("Ajuste");
        a.setMoneda("EUR");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.crear(a));

        assertTrue(ex.getMessage().contains("moneda"));
    }
}