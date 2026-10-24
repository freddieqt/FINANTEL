package com.finantel.duplicar_asiento.aceptacion;

import com.finantel.registrar_asiento_contable.aplicacion.AsientoContableService;
import com.finantel.registrar_asiento_contable.dominio.repository.AsientoContableRepository;
import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import com.finantel.registrar_accion.aplicacion.AuditoriaService;

import com.finantel.registrar_accion.aplicacion.AuditoriaService;
import com.finantel.registrar_asiento_contable.aplicacion.AsientoContableService;
import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import com.finantel.registrar_asiento_contable.dominio.repository.AsientoContableRepository;
import com.finantel.validar_cuentas.dominio.repository.ValidarCuentasRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CU06DuplicarAsientoAceptacionTest {

    @Mock private AsientoContableRepository repo;
    @Mock private AuditoriaService auditoria;
    @InjectMocks private AsientoContableService service;

    @Test
    void HA01_duplicar_no_modifica_el_original() {
        AsientoContable original = new AsientoContable();
        original.setId(1);
        original.setTipo("Operaci\u00f3n ordinaria");
        original.setDescripcion("Compra equipos");
        original.setDebe(8000.0);
        original.setHaber(8000.0);
        original.setEstado("Activo");
        original.setUsuario("admin@finantel.pe");
        original.setPeriodoId(3);
        when(repo.findById(1)).thenReturn(Optional.of(original));

        service.duplicar(1, "contador@finantel.pe");

        assertEquals("Compra equipos", original.getDescripcion());
        assertEquals("Activo", original.getEstado());
        assertEquals(8000.0, original.getDebe());
        verify(repo, times(1)).save(any());
    }
}
