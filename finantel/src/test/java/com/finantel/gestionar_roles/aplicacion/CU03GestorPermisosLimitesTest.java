package com.finantel.gestionar_roles.aplicacion;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CU03GestorPermisosLimitesTest {

    private final GestorPermisos gestor = new GestorPermisos();

    @Test
    void rol_vacio_no_tiene_permiso() {
        assertFalse(gestor.tienePermiso("", "ver"));
    }

    @Test
    void rol_solo_espacios_no_tiene_permiso() {
        assertFalse(gestor.tienePermiso("   ", "ver"));
    }

    @Test
    void rol_inexistente_no_tiene_permiso() {
        assertFalse(gestor.tienePermiso("Visitante", "ver"));
    }

    @Test
    void accion_vacia_no_tiene_permiso() {
        assertFalse(gestor.tienePermiso("Administrador", ""));
    }
}