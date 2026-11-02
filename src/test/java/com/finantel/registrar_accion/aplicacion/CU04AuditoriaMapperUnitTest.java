package com.finantel.registrar_accion.aplicacion;

import com.finantel.registrar_accion.infraestructura.mapping.AuditoriaMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CU04AuditoriaMapperUnitTest {

    private final AuditoriaMapper mapper = new AuditoriaMapper();

    @Test
    void toEntity_asigna_todos_los_campos_y_fecha_actual() {
        LocalDateTime antes = LocalDateTime.now();
        var a = mapper.toEntity("u", "Crear", "Asiento", "A", "B");
        assertEquals("u", a.getUsuario());
        assertEquals("Crear", a.getAccion());
        assertEquals("Asiento", a.getObjeto());
        assertEquals("A", a.getValorAnterior());
        assertEquals("B", a.getValorNuevo());
        assertNotNull(a.getFecha());
        assertFalse(a.getFecha().isBefore(antes));
    }
}