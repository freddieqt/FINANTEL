package com.finantel.registrar_accion.aplicacion;

import com.finantel.registrar_accion.infraestructura.persistence.AuditoriaConfig;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class CU04AuditoriaConfigUnitTest {

    private final AuditoriaConfig config = new AuditoriaConfig();

    @Test
    void contiene_login_exitoso() {
        assertTrue(Arrays.asList(config.getAccionesPermitidas()).contains("Login exitoso"));
    }

    @Test
    void textos_tienen_tildes_correctas() {
        assertTrue(Arrays.asList(config.getAccionesPermitidas()).contains("Creación"));
    }
}