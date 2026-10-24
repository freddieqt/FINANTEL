package com.finantel.duplicar_asiento.aplicacion;

import com.finantel.registrar_asiento_contable.aplicacion.AsientoContableService;
import com.finantel.registrar_asiento_contable.dominio.repository.AsientoContableRepository;
import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import com.finantel.registrar_accion.aplicacion.AuditoriaService;

import com.finantel.registrar_asiento_contable.dominio.repository.AsientoContableRepository;
import com.finantel.validar_cuentas.dominio.repository.ValidarCuentasRepository;

import com.finantel.registrar_accion.aplicacion.AuditoriaService;
import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CU06DuplicarAsientoEquivalenciaTest {

    @Mock private AsientoContableRepository repo;
    @Mock private AuditoriaService auditoria;
    @InjectMocks private AsientoContableService service;

    private AsientoContable original(String tipo) {
        AsientoContable o = new AsientoContable();
        o.setId(1);
        o.setTipo(tipo);
        o.setDescripcion("Original");
        o.setDebe(100.0);
        o.setHaber(100.0);
        o.setEstado("Activo");
        o.setUsuario("admin@finantel.pe");
        o.setCuentaContable("1011");
        o.setCentroCosto("General");
        o.setMoneda("PEN");
        o.setTipoCambio(1.0);
        o.setPeriodoId(3);
        return o;
    }

    @ParameterizedTest
    @ValueSource(strings = {"Apertura", "Operaci\u00f3n ordinaria", "Ajuste", "Cierre", "Reversi\u00f3n"})
    void duplicar_conserva_el_tipo_del_original(String tipo) {
        when(repo.findById(1)).thenReturn(Optional.of(original(tipo)));

        service.duplicar(1, "contador@finantel.pe");

        ArgumentCaptor<AsientoContable> captor = ArgumentCaptor.forClass(AsientoContable.class);
        verify(repo).save(captor.capture());
        assertEquals(tipo, captor.getValue().getTipo());
    }
}
