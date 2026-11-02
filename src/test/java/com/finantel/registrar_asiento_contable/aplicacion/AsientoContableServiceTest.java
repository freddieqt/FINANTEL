package com.finantel.registrar_asiento_contable.aplicacion;

import com.finantel.registrar_accion.aplicacion.AuditoriaService;
import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import com.finantel.registrar_asiento_contable.dominio.repository.AsientoContableRepository;
import com.finantel.validar_cuentas.dominio.entity.CuentaContable;
import com.finantel.validar_cuentas.dominio.repository.ValidarCuentasRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsientoContableServiceTest {

    @Mock AsientoContableRepository repo;
    @Mock AuditoriaService auditoriaService;
    @Mock ValidarCuentasRepository cuentaRepo;
    @InjectMocks AsientoContableService service;

    private CuentaContable cuentaActiva() {
        CuentaContable c = new CuentaContable();
        c.setEstado("Activa");
        return c;
    }

    private CuentaContable cuentaInactiva() {
        CuentaContable c = new CuentaContable();
        c.setEstado("Inactiva");
        return c;
    }

    private AsientoContable asientoValido() {
        AsientoContable a = new AsientoContable();
        a.setTipo("Operación ordinaria");
        a.setDescripcion("Cobro cuota crédito");
        a.setDebe(100.0);
        a.setHaber(100.0);
        a.setCuentaContable("1011");
        a.setMoneda("PEN");
        return a;
    }

    // CP-08: descripción vacía se rechaza y no se guarda
    @Test
    @SuppressWarnings("null")
    void cp08_descripcionVacia_noSeGuarda() {
        when(cuentaRepo.findById("1011")).thenReturn(Optional.of(cuentaActiva()));
        AsientoContable a = asientoValido();
        a.setDescripcion(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.crear(a));

        assertTrue(ex.getMessage().contains("Descripción es obligatoria"));
        verify(repo, never()).save(any());
    }

    // CP-09: asiento balanceado y completo se guarda y recibe correlativo
    @Test
    @SuppressWarnings("null")
    void cp09_asientoValido_seGuardaConCorrelativo() {
        when(cuentaRepo.findById("1011")).thenReturn(Optional.of(cuentaActiva()));
        AsientoContable a = asientoValido();

        assertDoesNotThrow(() -> service.crear(a));

        verify(repo).save(a);
        assertNotNull(a.getNumeroCorrelativo());
    }

    // CP-10: asiento descuadrado (100 vs 80) se rechaza y no se guarda
    @Test
    @SuppressWarnings("null")
    void cp10_asientoDescuadrado_noSeGuarda() {
        when(cuentaRepo.findById("1011")).thenReturn(Optional.of(cuentaActiva()));
        AsientoContable a = asientoValido();
        a.setHaber(80.0);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.crear(a));

        assertTrue(ex.getMessage().contains("no cuadra"));
        verify(repo, never()).save(any());
    }

    // CP-11: correlativo con formato AST-AÑO-NNN, año igual al actual
    @Test
    void cp11_correlativo_formatoYAnio() {
        when(cuentaRepo.findById("1011")).thenReturn(Optional.of(cuentaActiva()));
        AsientoContable a = asientoValido();

        service.crear(a);

        String esperado = "AST-" + LocalDate.now().getYear() + "-001";
        assertEquals(esperado, a.getNumeroCorrelativo());
    }

    // CP-12: correlativo que ya existe debe rechazarse (RF12)
    @Test
    @SuppressWarnings("null")
    void cp12_correlativoDuplicado_debeRechazarse() {
        when(cuentaRepo.findById("1011")).thenReturn(Optional.of(cuentaActiva()));
        when(repo.existsByNumeroCorrelativo(anyString())).thenReturn(true);
        
        AsientoContable a = asientoValido();

        assertThrows(RuntimeException.class, () -> service.crear(a));
        verify(repo, never()).save(any());
    }

    // CP-13: cuenta inactiva debe rechazarse (RF24/RF26)
    @Test
    @SuppressWarnings("null")
    void cp13_cuentaInactiva_debeRechazarse() {
        when(cuentaRepo.findById("1011")).thenReturn(Optional.of(cuentaInactiva()));
        
        AsientoContable a = asientoValido();

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.crear(a));

        assertTrue(ex.getMessage().contains("inactiva"));
        verify(repo, never()).save(any());
    }

    // CP-14: fechaRegistro se asigna automáticamente (RF16)
    @Test
    void cp14_fechaRegistro_seAsignaAutomaticamente() {
        when(cuentaRepo.findById("1011")).thenReturn(Optional.of(cuentaActiva()));
        AsientoContable a = asientoValido();

        service.crear(a);

        assertNotNull(a.getFechaRegistro());
    }
}