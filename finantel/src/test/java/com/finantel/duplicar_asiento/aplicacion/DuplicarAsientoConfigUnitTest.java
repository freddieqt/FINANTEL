package com.finantel.duplicar_asiento.aplicacion;

import com.finantel.duplicar_asiento.infraestructura.persistence.DuplicarAsientoConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DuplicarAsientoConfigUnitTest {

    private final DuplicarAsientoConfig config = new DuplicarAsientoConfig();

    @Test
    void puededuplicar_activo_es_true() {
        assertTrue(config.puededuplicar("Activo"));
    }

    @Test
    void puededuplicar_ignora_mayusculas() {
        assertTrue(config.puededuplicar("activo"));
    }

    @Test
    void puededuplicar_anulado_es_false() {
        assertFalse(config.puededuplicar("Anulado"));
    }

    @Test
    void puededuplicar_nulo_es_false() {
        assertFalse(config.puededuplicar(null));
    }
}