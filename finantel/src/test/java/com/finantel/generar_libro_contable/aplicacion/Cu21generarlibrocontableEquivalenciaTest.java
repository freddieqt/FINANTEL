package com.finantel.generar_libro_contable.aplicacion;

import com.finantel.generar_libro_contable.infraestructura.persistence.LibroContableConfig;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class CU21GenerarLibroContableEquivalenciaTest {

    private final LibroContableConfig config = new LibroContableConfig();

    @ParameterizedTest
    @ValueSource(strings = {"2026-10", "2026-01", "Octubre"})
    void CP_E1_periodos_con_texto_son_validos(String periodo) {
        assertTrue(config.esPeriodoValido(periodo));
    }

    @Test
    void CP_E2_periodo_vacio_no_es_valido() {
        assertFalse(config.esPeriodoValido(""));
    }
}