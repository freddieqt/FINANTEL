package com.finantel.generar_libro_contable.aplicacion;

import com.finantel.generar_libro_contable.infraestructura.persistence.LibroContableConfig;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CU21GenerarLibroContableLimitesTest {

    private final LibroContableConfig config = new LibroContableConfig();

    @Test
    void CP_L1_periodo_nulo_no_es_valido() {
        assertFalse(config.esPeriodoValido(null));
    }

    @Test
    void CP_L2_periodo_con_solo_espacios_no_es_valido() {
        assertFalse(config.esPeriodoValido("   "));
    }

    @Test
    void CP_L3_periodo_de_un_caracter_es_valido() {
        assertTrue(config.esPeriodoValido("2"));
    }
}