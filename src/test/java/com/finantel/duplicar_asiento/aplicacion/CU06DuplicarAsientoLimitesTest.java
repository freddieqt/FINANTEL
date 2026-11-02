package com.finantel.duplicar_asiento.aplicacion;

import com.finantel.registrar_asiento_contable.aplicacion.AsientoContableService;
import com.finantel.registrar_asiento_contable.dominio.repository.AsientoContableRepository;
import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import com.finantel.registrar_accion.aplicacion.AuditoriaService;

import com.finantel.registrar_asiento_contable.dominio.repository.AsientoContableRepository;
import com.finantel.validar_cuentas.dominio.repository.ValidarCuentasRepository;
import com.finantel.registrar_accion.aplicacion.AuditoriaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CU06DuplicarAsientoLimitesTest {

    @Mock private AsientoContableRepository repo;
    @Mock private AuditoriaService auditoria;
    @InjectMocks private AsientoContableService service;

    @Test
    void duplicar_con_id_nulo_lanza_excepcion_y_no_guarda() {
        assertThrows(RuntimeException.class, () -> service.duplicar(null, "contador@finantel.pe"));
        verify(repo, never()).save(any());
    }
}
