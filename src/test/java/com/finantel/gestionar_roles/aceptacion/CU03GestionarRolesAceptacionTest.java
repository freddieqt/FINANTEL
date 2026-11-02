package com.finantel.gestionar_roles.aceptacion;

import com.finantel.gestionar_roles.aplicacion.GestorPermisos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CU03GestionarRolesAceptacionTest {

    private final GestorPermisos gestor = new GestorPermisos();

    @Test
    void HA01_administrador_puede_gestionar_roles_y_permisos() {
        assertTrue(gestor.tienePermiso("Administrador", "crear"));
        assertTrue(gestor.tienePermiso("Administrador", "editar"));
        assertTrue(gestor.tienePermiso("Administrador", "ver"));
    }

    @Test
    void HA02_contador_general_crea_edita_y_consulta() {
        assertTrue(gestor.tienePermiso("Contador General", "crear"));
        assertTrue(gestor.tienePermiso("Contador General", "editar"));
        assertTrue(gestor.tienePermiso("Contador General", "ver"));
    }

    @Test
    void HA03_auditor_solo_puede_consultar() {
        assertFalse(gestor.tienePermiso("Auditor", "crear"));
        assertFalse(gestor.tienePermiso("Auditor", "editar"));
        assertTrue(gestor.tienePermiso("Auditor", "ver"));
    }

    @Test
    void HA04_personal_autorizado_solo_puede_consultar() {
        assertFalse(gestor.tienePermiso("Personal Autorizado", "crear"));
        assertFalse(gestor.tienePermiso("Personal Autorizado", "editar"));
        assertTrue(gestor.tienePermiso("Personal Autorizado", "ver"));
    }
}