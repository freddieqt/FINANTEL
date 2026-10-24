package com.finantel.inactivar_asiento.aplicacion;

import com.finantel.registrar_asiento_contable.aplicacion.AsientoContableService;
import com.finantel.registrar_asiento_contable.dominio.repository.AsientoContableRepository;
import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import com.finantel.registrar_accion.aplicacion.AuditoriaService;

// Copia aquí los imports de repositorios de CU01RegistrarAsientoEquivalenciaTest.java
// (AsientoContableRepository)

import com.finantel.registrar_accion.aplicacion.AuditoriaService;
import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CU05AnularAsientoEquivalenciaLimitesTest {

    @Mock private AsientoContableRepository repo;
    @Mock private AuditoriaService auditoria;
    @InjectMocks private AsientoContableService service;

    private AsientoContable asiento(String tipo) {
        AsientoContable a = new AsientoContable();
        a.setId(1);
        a.setTipo(tipo);
        a.setEstado("Activo");
        a.setUsuario("admin@finantel.pe");
        return a;
    }

    @ParameterizedTest
    @ValueSource(strings = {"Apertura", "Operaci\u00f3n ordinaria", "Ajuste", "Cierre", "Reversi\u00f3n"})
    void anular_funciona_para_cualquier_tipo_de_asiento(String tipo) {
        AsientoContable a = asiento(tipo);
        when(repo.findById(1)).thenReturn(Optional.of(a));

        service.anular(1);

        assertEquals("Anulado", a.getEstado());
    }

    @Test
    void anular_con_id_nulo_lanza_excepcion_y_no_guarda() {
        assertThrows(RuntimeException.class, () -> service.anular(null));
        verify(repo, never()).save(any());
    }
}