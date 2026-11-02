package com.finantel.conciliacion_bancaria.aplicacion;

import com.finantel.conciliacion_bancaria.infraestructura.persistence.ConciliacionBancariaConfig;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class CU20ConciliacionBancariaEquivalenciaTest {

    private final ConciliacionBancariaConfig config = new ConciliacionBancariaConfig();

    @ParameterizedTest
    @ValueSource(doubles = {0.0, 0.005, -0.005})
    void CP_E1_diferencias_casi_nulas_estan_conciliadas(double diferencia) {
        assertTrue(config.estaConciliado(diferencia));
    }

    @ParameterizedTest
    @ValueSource(doubles = {1.0, -1.0, 25.5})
    void CP_E2_diferencias_significativas_no_estan_conciliadas(double diferencia) {
        assertFalse(config.estaConciliado(diferencia));
    }
}