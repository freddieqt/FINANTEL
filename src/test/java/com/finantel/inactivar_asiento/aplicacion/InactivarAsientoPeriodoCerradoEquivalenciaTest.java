package com.finantel.inactivar_asiento.aplicacion;


import com.finantel.registrar_accion.aplicacion.AuditoriaService;
import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import com.finantel.registrar_asiento_contable.dominio.repository.AsientoContableRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InactivarAsientoPeriodoCerradoEquivalenciaTest {

    @Mock AsientoContableRepository repo;
    @Mock AuditoriaService auditoriaService;
    @InjectMocks InactivarAsientoService service;

    // ENTRADA: asiento id = 1 con estado "Cerrado" (periodo cerrado)
    // RESULTADO: Se rechaza con el mensaje "No se puede inactivar un asiento en periodo cerrado" y no se guarda
    @Test
    void asiento_de_periodo_cerrado_no_se_inactiva() {
        AsientoContable a = new AsientoContable();
        a.setEstado("Cerrado");
        when(repo.findById(1)).thenReturn(Optional.of(a));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.inactivar(1));
        assertTrue(ex.getMessage().contains("periodo cerrado"));
        verify(repo, never()).save(any());
    }

    // ENTRADA: asiento id = 1 con estado "Activo"
    // RESULTADO: El asiento se inactiva sin errores
    @Test
    void asiento_activo_se_inactiva() {
        AsientoContable a = new AsientoContable();
        a.setEstado("Activo");
        when(repo.findById(1)).thenReturn(Optional.of(a));
        assertDoesNotThrow(() -> service.inactivar(1));
    }

    private static AsientoContable any() {
        return org.mockito.ArgumentMatchers.any();
    }
}
