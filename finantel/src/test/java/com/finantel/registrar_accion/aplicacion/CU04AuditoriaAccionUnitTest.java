package com.finantel.registrar_accion.aplicacion;

import com.finantel.registrar_accion.dominio.entity.AuditoriaAccion;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CU04AuditoriaAccionUnitTest {

    @Test
    void setters_y_getters_guardan_todos_los_campos() {
        LocalDateTime fecha = LocalDateTime.of(2026, 10, 22, 21, 0);
        AuditoriaAccion a = new AuditoriaAccion();
        a.setId(1);
        a.setFecha(fecha);
        a.setUsuario("admin@finantel.pe");
        a.setAccion("Crear asiento");
        a.setObjeto("AsientosContables");
        a.setValorAnterior("A");
        a.setValorNuevo("B");

        assertEquals(1, a.getId());
        assertEquals(fecha, a.getFecha());
        assertEquals("admin@finantel.pe", a.getUsuario());
        assertEquals("Crear asiento", a.getAccion());
        assertEquals("AsientosContables", a.getObjeto());
        assertEquals("A", a.getValorAnterior());
        assertEquals("B", a.getValorNuevo());
    }
}