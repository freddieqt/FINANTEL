package com.finantel.registrar_asiento_contable.aplicacion;

import com.finantel.registrar_accion.aplicacion.AuditoriaService;
import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import com.finantel.registrar_asiento_contable.dominio.repository.AsientoContableRepository;
import com.finantel.registrar_asiento_contable.infraestructura.persistence.AsientoContableConfig;
import com.finantel.validar_cuentas.dominio.entity.CuentaContable;
import com.finantel.validar_cuentas.dominio.repository.ValidarCuentasRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CU01RegistrarAsientoLimitesTest {

    private final AsientoContableConfig config = new AsientoContableConfig();

    @Mock
    private AsientoContableRepository repo;

    @Mock
    private AuditoriaService auditoriaService;

    @Mock
    private ValidarCuentasRepository cuentaRepo;

    @InjectMocks
    private AsientoContableService service;

    private AsientoContable conDebeHaber(Double debe, Double haber) {
        AsientoContable a = new AsientoContable();
        a.setTipo("Ajuste");
        a.setDescripcion("Cobro cuota");
        a.setDebe(debe);
        a.setHaber(haber);
        a.setCuentaContable("1011");
        a.setMoneda("PEN");
        return a;
    }

    private void prepararCuenta(String estado) {
        CuentaContable cuenta = new CuentaContable();
        cuenta.setEstado(estado);
        lenient().when(cuentaRepo.findById("1011")).thenReturn(Optional.of(cuenta));
        lenient().when(repo.count()).thenReturn(0L);
        lenient().when(repo.existsByNumeroCorrelativo(anyString())).thenReturn(false);
    }

    @Test
    void CP_L1_debe_igual_a_cero_se_rechaza() {
        prepararCuenta("Activa");

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.crear(conDebeHaber(0.0, 5000.0)));

        assertTrue(ex.getMessage().contains("debe"));
    }

    @Test
    void CP_L2_diferencia_de_0_01_cuadra() {
        prepararCuenta("Activa");

        assertDoesNotThrow(() -> service.crear(conDebeHaber(5000.00, 5000.01)));
    }

    @Test
    void CP_L3_diferencia_de_0_02_no_cuadra() {
        prepararCuenta("Activa");

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.crear(conDebeHaber(5000.00, 5000.02)));

        assertTrue(ex.getMessage().contains("no cuadra"));
    }

    @Test
    void CP_L4_haber_nulo_se_rechaza() {
        prepararCuenta("Activa");

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.crear(conDebeHaber(5000.0, null)));

        assertTrue(ex.getMessage().contains("haber"));
    }

    @Test
    void CP_L5_cuenta_en_blanco_se_rechaza() {
        AsientoContable a = conDebeHaber(5000.0, 5000.0);
        a.setCuentaContable("   ");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.crear(a));

        assertTrue(ex.getMessage().contains("obligatoria"));
    }

    @Test
    void CP_L6_cuenta_inactiva_se_rechaza() {
        prepararCuenta("Inactiva");

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.crear(conDebeHaber(5000.0, 5000.0)));

        assertTrue(ex.getMessage().contains("inactiva"));
    }

    @Test
    void CP_L7_periodo_anulado_se_detecta_sin_importar_mayusculas() {
        assertTrue(config.esAnulado("anulado"));
    }
}