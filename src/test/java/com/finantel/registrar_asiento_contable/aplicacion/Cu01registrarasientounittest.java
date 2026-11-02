package com.finantel.registrar_asiento_contable.aplicacion;

import com.finantel.registrar_accion.aplicacion.AuditoriaService;
import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import com.finantel.registrar_asiento_contable.dominio.repository.AsientoContableRepository;
import com.finantel.validar_cuentas.dominio.entity.CuentaContable;
import com.finantel.validar_cuentas.dominio.repository.ValidarCuentasRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CU01RegistrarAsientoUnitTest {

    @Mock
    private AsientoContableRepository repo;

    @Mock
    private AuditoriaService auditoriaService;

    @Mock
    private ValidarCuentasRepository cuentaRepo;

    @InjectMocks
    private AsientoContableService service;

    private AsientoContable valido() {
        AsientoContable a = new AsientoContable();
        a.setTipo("Ajuste");
        a.setDescripcion("Cobro cuota");
        a.setDebe(5000.0);
        a.setHaber(5000.0);
        a.setCuentaContable("1011");
        a.setMoneda("PEN");
        a.setUsuario("ana@finantel.pe");
        return a;
    }

    private void prepararCuentaActiva() {
        CuentaContable cuenta = new CuentaContable();
        cuenta.setCodigo("1011");
        cuenta.setEstado("Activa");
        lenient().when(cuentaRepo.findById("1011")).thenReturn(Optional.of(cuenta));
        lenient().when(repo.count()).thenReturn(0L);
        lenient().when(repo.existsByNumeroCorrelativo(anyString())).thenReturn(false);
    }

    private void cuentaActivaExiste(String codigo) {
        CuentaContable c = new CuentaContable();
        c.setCodigo(codigo);
        c.setEstado("Activa");
        lenient().when(cuentaRepo.findById(codigo)).thenReturn(Optional.of(c));
        lenient().when(repo.count()).thenReturn(0L);
        lenient().when(repo.existsByNumeroCorrelativo(anyString())).thenReturn(false);
    }

    private AsientoContable asiento(String tipo, String descripcion, Double debe, Double haber,
                                    String cuenta, String moneda) {
        AsientoContable a = new AsientoContable();
        a.setTipo(tipo);
        a.setDescripcion(descripcion);
        a.setDebe(debe);
        a.setHaber(haber);
        a.setCuentaContable(cuenta);
        a.setMoneda(moneda);
        a.setUsuario("ana@finantel.pe");
        return a;
    }

    @Test
    void CP_U1_asiento_valido_se_guarda_como_activo() {
        prepararCuentaActiva();

        service.crear(valido());

        ArgumentCaptor<AsientoContable> captor = ArgumentCaptor.forClass(AsientoContable.class);
        verify(repo).save(captor.capture());
        assertEquals("Activo", captor.getValue().getEstado());
    }

    @Test
    void CP_U2_el_asiento_recibe_un_correlativo_que_termina_en_001() {
        prepararCuentaActiva();

        service.crear(valido());

        ArgumentCaptor<AsientoContable> captor = ArgumentCaptor.forClass(AsientoContable.class);
        verify(repo).save(captor.capture());
        assertTrue(captor.getValue().getNumeroCorrelativo().startsWith("AST-"));
        assertTrue(captor.getValue().getNumeroCorrelativo().endsWith("-001"));
    }

    @Test
    void CP_U3_correlativo_duplicado_no_se_guarda() {
        prepararCuentaActiva();
        when(repo.existsByNumeroCorrelativo(anyString())).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.crear(valido()));

        assertTrue(ex.getMessage().contains("ya existe"));
        verify(repo, never()).save(any());
    }

    // ENTRADA: tipo = "Operación ordinaria"; descripción = "Cobro cuota crédito"; debe = haber = 100.0; cuenta = 1201; moneda = PEN
    // RESULTADO: El asiento se registra sin errores
    @Test
    void EQ_CU01_tipo_operacion_ordinaria_se_registra() {
        cuentaActivaExiste("1201");
        assertDoesNotThrow(() -> service.crear(asiento("Operación ordinaria", "Cobro cuota crédito", 100.0, 100.0, "1201", "PEN")));
    }

    // ENTRADA: tipo = "Reversión"; descripción = "Cobro cuota crédito"; debe = haber = 100.0; cuenta = 1201; moneda = PEN
    // RESULTADO: El asiento se registra sin errores
    @Test
    void EQ_CU01_tipo_reversion_se_registra() {
        cuentaActivaExiste("1201");
        assertDoesNotThrow(() -> service.crear(asiento("Reversión", "Cobro cuota crédito", 100.0, 100.0, "1201", "PEN")));
    }

    // ENTRADA: tipo = null; descripción = "Cobro cuota crédito"; debe = haber = 100.0; cuenta = 1201; moneda = PEN
    // RESULTADO: Se rechaza con el mensaje "Tipo inválido"
    @Test
    void EQ_CU01_tipo_vacio_se_rechaza() {
        cuentaActivaExiste("1201");
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.crear(asiento(null, "Cobro cuota crédito", 100.0, 100.0, "1201", "PEN")));
        assertTrue(ex.getMessage().contains("Tipo inválido"));
    }

    // ENTRADA: tipo = "Ajuste"; descripción = "    " (solo espacios); debe = haber = 100.0; cuenta = 1201; moneda = PEN
    // RESULTADO: Se rechaza con el mensaje "Descripción es obligatoria"
    @Test
    void EQ_CU01_descripcion_en_blanco_se_rechaza() {
        cuentaActivaExiste("1201");
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.crear(asiento("Ajuste", "    ", 100.0, 100.0, "1201", "PEN")));
        assertTrue(ex.getMessage().contains("Descripción es obligatoria"));
    }

    // ENTRADA: tipo = "Ajuste"; descripción = "Cobro cuota crédito"; debe = haber = 100.0; cuenta = "" (vacía); moneda = PEN
    // RESULTADO: Se rechaza con el mensaje "La cuenta contable es obligatoria"
    @Test
    void EQ_CU01_cuenta_vacia_se_rechaza() {
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.crear(asiento("Ajuste", "Cobro cuota crédito", 100.0, 100.0, "", "PEN")));
        assertTrue(ex.getMessage().contains("La cuenta contable es obligatoria"));
    }

    // ENTRADA: tipo = "Ajuste"; descripción = "Cobro cuota crédito"; debe = haber = 100.0; cuenta = "9999" (no existe); moneda = PEN
    // RESULTADO: Se rechaza con el mensaje "no existe"
    @Test
    void EQ_CU01_cuenta_inexistente_se_rechaza() {
        lenient().when(cuentaRepo.findById("9999")).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.crear(asiento("Ajuste", "Cobro cuota crédito", 100.0, 100.0, "9999", "PEN")));
        assertTrue(ex.getMessage().contains("no existe"));
    }

    // ENTRADA: tipo = "Ajuste"; descripción = "Cobro cuota crédito"; debe = 100.0; haber = 99.98; cuenta = 1201; moneda = PEN
    // RESULTADO: Se rechaza con el mensaje "no cuadra" (diferencia de 0.02 supera la tolerancia de 0.01)
    @Test
    void EQ_CU01_cuadre_con_diferencia_de_0_02_se_rechaza() {
        cuentaActivaExiste("1201");
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.crear(asiento("Ajuste", "Cobro cuota crédito", 100.0, 99.98, "1201", "PEN")));
        assertTrue(ex.getMessage().contains("no cuadra"));
    }

    // ENTRADA: tipo = "Ajuste"; descripción = "Cobro cuota crédito"; debe = 0.0; haber = 100.0; cuenta = 1201; moneda = PEN
    // RESULTADO: Se rechaza con el mensaje "debe" mayor a 0
    @Test
    void EQ_CU01_debe_en_cero_se_rechaza() {
        cuentaActivaExiste("1201");
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.crear(asiento("Ajuste", "Cobro cuota crédito", 0.0, 100.0, "1201", "PEN")));
        assertTrue(ex.getMessage().contains("'debe' debe ser mayor a 0"));
    }

    // ENTRADA: tipo = "Ajuste"; descripción = "Cobro cuota crédito"; debe = haber = 100.0; cuenta = 1201; moneda = "EUR"
    // RESULTADO: Se rechaza con el mensaje "La moneda es obligatoria"
    @Test
    void EQ_CU01_moneda_fuera_de_PEN_USD_se_rechaza() {
        cuentaActivaExiste("1201");
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.crear(asiento("Ajuste", "Cobro cuota crédito", 100.0, 100.0, "1201", "EUR")));
        assertTrue(ex.getMessage().contains("La moneda es obligatoria"));
    }
}