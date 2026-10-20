package com.finantel.registrar_asiento_contable.aplicacion;

import com.finantel.registrar_accion.aplicacion.AuditoriaService;
import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import com.finantel.registrar_asiento_contable.dominio.repository.AsientoContableRepository;
import com.finantel.validar_cuentas.dominio.repository.ValidarCuentasRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsientoContableOperacionesTest {

    @Mock AsientoContableRepository repo;
    @Mock AuditoriaService auditoriaService;
    @Mock ValidarCuentasRepository cuentaRepo;
    @InjectMocks AsientoContableService service;

    private AsientoContable asientoOriginal() {
        AsientoContable a = new AsientoContable();
        a.setId(1);
        a.setTipo("Operación ordinaria");
        a.setDescripcion("Cobro cuota crédito");
        a.setDebe(100.0);
        a.setHaber(100.0);
        a.setCuentaContable("1011");
        a.setMoneda("PEN");
        a.setUsuario("fredi@finantel.pe");
        a.setEstado("Activo");
        a.setNumeroCorrelativo("AST-2026-001");
        return a;
    }

    // Escenario: editar un asiento y mantener el cuadre (RF10, RF13)
    @Test
    @SuppressWarnings("null")
    void ed01_editarAsientoDescuadrado_debeRechazarse() {
        AsientoContable original = asientoOriginal();
        when(repo.findById(1)).thenReturn(Optional.of(original));

        AsientoContable cambios = new AsientoContable();
        cambios.setDebe(150.0); // haber queda en 100 -> descuadrado

        assertThrows(RuntimeException.class, () -> service.actualizar(1, cambios));
        verify(repo, never()).save(any());
    }

    // Escenario: duplicar un asiento genera una copia con su propio correlativo (RF15)
    @Test
    @SuppressWarnings("null")
    void du01_duplicarAsiento_copiaTieneCorrelativoNuevo() {
        AsientoContable original = asientoOriginal();
        when(repo.findById(1)).thenReturn(Optional.of(original));
        when(repo.count()).thenReturn(1L);

        service.duplicar(1, "alexis@finantel.pe");

        ArgumentCaptor<AsientoContable> captor = ArgumentCaptor.forClass(AsientoContable.class);
        verify(repo).save(captor.capture());
        AsientoContable copia = captor.getValue();
        assertNotNull(copia.getNumeroCorrelativo());
        assertNotEquals(original.getNumeroCorrelativo(), copia.getNumeroCorrelativo());
    }

    // Escenario: anular un asiento cambia su estado a "Anulado" (control, debe pasar)
    @Test
    void an01_anularAsiento_cambiaEstadoAAnulado() {
        AsientoContable original = asientoOriginal();
        when(repo.findById(1)).thenReturn(Optional.of(original));

        service.anular(1);

        assertEquals("Anulado", original.getEstado());
        verify(repo).save(original);
    }

    // PO-01: editar la descripción de un asiento válido se guarda y se audita
    @Test
    void po01_editarDescripcionValida_seGuardaYAudita() {
        AsientoContable original = asientoOriginal();
        when(repo.findById(1)).thenReturn(Optional.of(original));

        AsientoContable cambios = new AsientoContable();
        cambios.setTipo("Operación ordinaria");
        cambios.setDescripcion("Cobro cuota corregido");

        service.actualizar(1, cambios);

        assertEquals("Cobro cuota corregido", original.getDescripcion());
        verify(repo).save(original);
        verify(auditoriaService).registrar(any(), any(), any(), any(), any());
    }

    // ED-02: editar con debe = 0 debe rechazarse (límite)
    @Test
    @SuppressWarnings("null")
    void ed02_editarConDebeCero_debeRechazarse() {
        AsientoContable original = asientoOriginal();
        when(repo.findById(1)).thenReturn(Optional.of(original));

        AsientoContable cambios = new AsientoContable();
        cambios.setDebe(0.0);
        cambios.setHaber(0.0);

        assertThrows(RuntimeException.class, () -> service.actualizar(1, cambios));
        verify(repo, never()).save(any());
    }

    // AN-02: anular un asiento que no existe debe rechazarse (negativa)
    @Test
    @SuppressWarnings("null")
    void an02_anularAsientoInexistente_debeRechazarse() {
        when(repo.findById(99)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.anular(99));

        assertTrue(ex.getMessage().contains("no encontrado"));
        verify(repo, never()).save(any());
    }
}