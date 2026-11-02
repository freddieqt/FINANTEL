package com.finantel.gestionar_roles.aplicacion;

import com.finantel.gestionar_roles.dominio.entity.Rol;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CU03RolUnitTest {

    @Test
    void constructor_asigna_nombre_y_descripcion() {
        Rol r = new Rol("Auditor", "Solo consulta");
        assertEquals("Auditor", r.getNombre());
        assertEquals("Solo consulta", r.getDescripcion());
    }

    @Test
    void setters_actualizan_nombre_y_descripcion() {
        Rol r = new Rol("Auditor", "Solo consulta");
        r.setNombre("Auditor Interno");
        r.setDescripcion("Consulta y reportes");
        assertEquals("Auditor Interno", r.getNombre());
        assertEquals("Consulta y reportes", r.getDescripcion());
    }
}