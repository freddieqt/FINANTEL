package com.finantel.detectar_inconsistencias.aplicacion;

import com.finantel.detectar_inconsistencias.dominio.entity.Inconsistencia;
import com.finantel.detectar_inconsistencias.dominio.repository.InconsistenciaRepository;
import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CU09DetectarInconsistenciasUnitTest {

    @Mock
    private InconsistenciaRepository repo;

    @InjectMocks
    private DetectarInconsistenciasService service;

    private AsientoContable asiento(double debe, double haber) {
        AsientoContable a = new AsientoContable();
        a.setDebe(debe);
        a.setHaber(haber);
        return a;
    }

    @Test
    void CP_U1_asiento_descuadrado_se_reporta_como_error() {
        when(repo.findById(1)).thenReturn(Optional.of(asiento(100.0, 50.0)));

        String resultado = service.detectarErrores(1);

        assertTrue(resultado.startsWith("Error:"));
        assertTrue(resultado.contains("Debe: 100.0 Haber: 50.0"));
    }

    @Test
    void CP_U2_asiento_cuadrado_no_reporta_inconsistencias() {
        when(repo.findById(2)).thenReturn(Optional.of(asiento(100.0, 100.0)));

        assertEquals("Sin inconsistencias detectadas", service.detectarErrores(2));
    }

    @Test
    void CP_U3_detectarTodos_reporta_solo_los_asientos_descuadrados() {
        when(repo.findAll()).thenReturn(List.of(asiento(100.0, 100.0), asiento(100.0, 50.0)));

        List<Inconsistencia> errores = service.detectarTodos();

        assertEquals(1, errores.size());
        assertEquals("Desbalance contable", errores.get(0).getTipoError());
    }
}