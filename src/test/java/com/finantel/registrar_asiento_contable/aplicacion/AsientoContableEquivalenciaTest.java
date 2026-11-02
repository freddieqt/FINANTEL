package com.finantel.registrar_asiento_contable.aplicacion;

import com.finantel.registrar_accion.aplicacion.AuditoriaService;
import com.finantel.registrar_asiento_contable.aplicacion.AsientoContableService;
import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import com.finantel.registrar_asiento_contable.dominio.repository.AsientoContableRepository;
import com.finantel.validar_cuentas.dominio.entity.CuentaContable;
import com.finantel.validar_cuentas.dominio.repository.ValidarCuentasRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class AsientoContableEquivalenciaTest {

    @Mock AsientoContableRepository repo;
    @Mock AuditoriaService auditoria;
    @Mock ValidarCuentasRepository cuentaRepo;
    @InjectMocks AsientoContableService service;

    private void cuentaActivaExiste(String codigo) {
        CuentaContable c = new CuentaContable();
        c.setCodigo(codigo);
        c.setEstado("Activa");
        lenient().when(cuentaRepo.findById(codigo)).thenReturn(Optional.of(c));
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

    // ENTRADA: tipo = "Ajuste"; descripción = "   " (solo espacios); debe = haber = 100.0; cuenta = 1201; moneda = PEN
    // RESULTADO: Se rechaza con el mensaje "Descripción es obligatoria"
    @Test
    void EQ_CU01_descripcion_en_blanco_se_rechaza() {
        cuentaActivaExiste("1201");
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.crear(asiento("Ajuste", "   ", 100.0, 100.0, "1201", "PEN")));
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
