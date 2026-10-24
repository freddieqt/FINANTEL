package com.finantel.gestionar_roles.aplicacion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class CU03GestorPermisosUnitTest {

    private final GestorPermisos gestor = new GestorPermisos();

    @Test
    void rol_nulo_no_tiene_permiso() {
        assertFalse(gestor.tienePermiso(null, "ver"));
    }

    @Test
    void accion_nula_no_tiene_permiso() {
        assertFalse(gestor.tienePermiso("Administrador", null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"crear", "editar", "ver"})
    void administrador_tiene_todos_los_permisos(String accion) {
        assertTrue(gestor.tienePermiso("Administrador", accion));
    }

    @ParameterizedTest
    @CsvSource({
        "Contador General,crear,true",
        "Contador General,editar,true",
        "Contador General,ver,true",
        "Auditor,crear,false",
        "Auditor,editar,false",
        "Auditor,ver,true",
        "Personal Autorizado,crear,false",
        "Personal Autorizado,editar,false",
        "Personal Autorizado,ver,true"
    })
    void matriz_de_permisos_por_rol(String rol, String accion, boolean esperado) {
        assertEquals(esperado, gestor.tienePermiso(rol, accion));
    }
}