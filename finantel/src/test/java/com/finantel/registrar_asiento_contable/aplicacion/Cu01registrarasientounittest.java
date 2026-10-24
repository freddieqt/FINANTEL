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
        return a;
    }

    private void prepararCuentaActiva() {
        CuentaContable cuenta = new CuentaContable();
        cuenta.setEstado("Activa");
        lenient().when(cuentaRepo.findById("1011")).thenReturn(Optional.of(cuenta));
        lenient().when(repo.count()).thenReturn(0L);
        lenient().when(repo.existsByNumeroCorrelativo(anyString())).thenReturn(false);
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
}