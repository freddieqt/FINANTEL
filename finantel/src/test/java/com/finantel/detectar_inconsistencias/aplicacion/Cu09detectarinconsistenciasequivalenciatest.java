package com.finantel.detectar_inconsistencias.aplicacion;

import com.finantel.detectar_inconsistencias.infraestructura.persistence.InconsistenciaConfig;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class CU09DetectarInconsistenciasEquivalenciaTest {

    private final InconsistenciaConfig config = new InconsistenciaConfig();

    @ParameterizedTest
    @CsvSource({
        "100.0, 100.0, true",
        "0.0, 0.0, true",
        "100.0, 50.0, false",
        "50.0, 100.0, false"
    })
    void CP_E1_clases_de_debe_y_haber_se_clasifican_correctamente(double debe, double haber, boolean esperado) {
        assertEquals(esperado, config.estaBalanceado(debe, haber));
    }
}