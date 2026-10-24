package com.finantel.realizar_copia_seguridad.aplicacion;

import com.finantel.realizar_copia_seguridad.infraestructura.persistence.CopiaSeguridadConfig;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class CU16RealizarCopiaSeguridadEquivalenciaTest {

    private final CopiaSeguridadConfig config = new CopiaSeguridadConfig();

    @ParameterizedTest
    @ValueSource(strings = {"Administrador", "ADMINISTRADOR", "administrador"})
    void CP_E1_roles_administrador_tienen_permiso(String rol) {
        assertTrue(config.tienePermisos(rol));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Auditor", "Contador", "Usuario"})
    void CP_E2_otros_roles_no_tienen_permiso(String rol) {
        assertFalse(config.tienePermisos(rol));
    }
}