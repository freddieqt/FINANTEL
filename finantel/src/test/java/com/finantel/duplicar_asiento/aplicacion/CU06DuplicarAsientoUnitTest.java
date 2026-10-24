package com.finantel.duplicar_asiento.aplicacion;

import com.finantel.registrar_asiento_contable.aplicacion.AsientoContableService;
import com.finantel.registrar_asiento_contable.dominio.repository.AsientoContableRepository;
import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import com.finantel.registrar_accion.aplicacion.AuditoriaService;

import com.finantel.registrar_asiento_contable.dominio.repository.AsientoContableRepository;
import com.finantel.validar_cuentas.dominio.repository.ValidarCuentasRepository;

import com.finantel.registrar_accion.aplicacion.AuditoriaService;
import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CU06DuplicarAsientoUnitTest {

    @Mock private AsientoContableRepository repo;
    @Mock private AuditoriaService auditoria;
    @InjectMocks private AsientoContableService service;

    private AsientoContable original() {
        AsientoContable o = new AsientoContable();
        o.setId(1);
        o.setTipo("Operaci\u00f3n ordinaria");
        o.setDescripcion("Compra equipos");
        o.setDebe(8000.0);
        o.setHaber(8000.0);
        o.setEstado("Activo");
        o.setUsuario("admin@finantel.pe");
        o.setCuentaContable("1401");
        o.setCentroCosto("General");
        o.setMoneda("PEN");
        o.setTipoCambio(1.0);
        o.setPeriodoId(3);
        return o;
    }

    private AsientoContable guardado() {
        ArgumentCaptor<AsientoContable> captor = ArgumentCaptor.forClass(AsientoContable.class);
        verify(repo).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void duplicar_copia_los_campos_del_original() {
        when(repo.findById(1)).thenReturn(Optional.of(original()));

        service.duplicar(1, "contador@finantel.pe");

        AsientoContable c = guardado();
        assertEquals("Operaci\u00f3n ordinaria", c.getTipo());
        assertEquals(8000.0, c.getDebe());
        assertEquals(8000.0, c.getHaber());
        assertEquals("1401", c.getCuentaContable());
        assertEquals("General", c.getCentroCosto());
        assertEquals("PEN", c.getMoneda());
        assertEquals(1.0, c.getTipoCambio());
        assertEquals(3, c.getPeriodoId());
    }

    @Test
    void duplicar_marca_la_copia_activa_y_con_el_usuario_indicado() {
        when(repo.findById(1)).thenReturn(Optional.of(original()));

        service.duplicar(1, "contador@finantel.pe");

        AsientoContable c = guardado();
        assertEquals("Activo", c.getEstado());
        assertEquals("contador@finantel.pe", c.getUsuario());
    }

    @Test
    void duplicar_antepone_COPIA_a_la_descripcion() {
        when(repo.findById(1)).thenReturn(Optional.of(original()));

        service.duplicar(1, "contador@finantel.pe");

        assertEquals("COPIA - Compra equipos", guardado().getDescripcion());
    }

    @Test
    void duplicar_genera_correlativo_con_formato_AST() {
        when(repo.findById(1)).thenReturn(Optional.of(original()));

        service.duplicar(1, "contador@finantel.pe");

        assertTrue(guardado().getNumeroCorrelativo().matches("AST-\\d{4}-\\d{3}"));
    }

    @Test
    void duplicar_con_correlativo_existente_lanza_excepcion_y_no_guarda() {
        when(repo.findById(1)).thenReturn(Optional.of(original()));
        when(repo.existsByNumeroCorrelativo(anyString())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> service.duplicar(1, "contador@finantel.pe"));
        verify(repo, never()).save(any());
    }

    @Test
    void duplicar_registra_auditoria() {
        when(repo.findById(1)).thenReturn(Optional.of(original()));

        service.duplicar(1, "contador@finantel.pe");

        verify(auditoria).registrar(eq("contador@finantel.pe"), anyString(),
                eq("AsientosContables"), eq("1"), startsWith("COPIA - "));
    }

    @Test
    void duplicar_id_inexistente_lanza_excepcion() {
        when(repo.findById(9)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.duplicar(9, "contador@finantel.pe"));
        verify(repo, never()).save(any());
    }
}
