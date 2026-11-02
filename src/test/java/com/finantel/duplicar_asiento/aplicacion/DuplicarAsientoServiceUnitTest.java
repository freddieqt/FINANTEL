package com.finantel.duplicar_asiento.aplicacion;

import com.finantel.registrar_accion.aplicacion.AuditoriaService;
import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import com.finantel.registrar_asiento_contable.dominio.repository.AsientoContableRepository;
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
class DuplicarAsientoServiceUnitTest {

    @Mock private AsientoContableRepository repo;
    @Mock private AuditoriaService auditoria;
    @InjectMocks private DuplicarAsientoService service;

    private AsientoContable original() {
        AsientoContable o = new AsientoContable();
        o.setId(1);
        o.setDescripcion("Compra equipos");
        o.setTipo("Operaci\u00f3n ordinaria");
        o.setDebe(8000.0);
        o.setHaber(8000.0);
        o.setEstado("Activo");
        return o;
    }

    private AsientoContable guardado() {
        ArgumentCaptor<AsientoContable> captor = ArgumentCaptor.forClass(AsientoContable.class);
        verify(repo).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void duplicar_antepone_COPIA_y_copia_montos_y_tipo() {
        when(repo.findById(1)).thenReturn(Optional.of(original()));

        service.duplicar(1, "contador@finantel.pe");

        AsientoContable c = guardado();
        assertEquals("COPIA - Compra equipos", c.getDescripcion());
        assertEquals("Operaci\u00f3n ordinaria", c.getTipo());
        assertEquals(8000.0, c.getDebe());
        assertEquals(8000.0, c.getHaber());
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
    void duplicar_registra_auditoria() {
        when(repo.findById(1)).thenReturn(Optional.of(original()));

        service.duplicar(1, "contador@finantel.pe");

        verify(auditoria).registrar(eq("contador@finantel.pe"), anyString(),
                eq("AsientosContables"), eq("Asiento #1"), eq("COPIA - Compra equipos"));
    }

    @Test
    void duplicar_id_inexistente_lanza_excepcion_y_no_guarda() {
        when(repo.findById(9)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.duplicar(9, "contador@finantel.pe"));
        verify(repo, never()).save(any());
    }
}