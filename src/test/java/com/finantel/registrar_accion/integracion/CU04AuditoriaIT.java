package com.finantel.registrar_accion.integracion;

import com.finantel.registrar_accion.aplicacion.AuditoriaService;
import com.finantel.registrar_accion.dominio.repository.AuditoriaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CU04AuditoriaIT {

    private static final String USUARIO = "ci-test@finantel.pe";

    @Autowired private AuditoriaService servicio;
    @Autowired private AuditoriaRepository repo;

    @Test
    void registrar_persiste_la_accion_en_la_base() {
        servicio.registrar(USUARIO, "Crear asiento", "AsientosContables", "-", "AST-1");
        var guardados = repo.findByUsuario(USUARIO);
        assertEquals(1, guardados.size());
        assertEquals("Crear asiento", guardados.get(0).getAccion());
        assertNotNull(guardados.get(0).getFecha());
    }

    @Test
    void filtrarPorAccion_devuelve_la_accion_pedida() {
        servicio.registrar(USUARIO, "Crear asiento", "AsientosContables", "-", "AST-1");
        servicio.registrar(USUARIO, "Inactivar asiento", "AsientosContables", "Activo", "Inactivo");
        long encontrados = servicio.filtrarPorAccion("Inactivar asiento").stream()
                .filter(a -> USUARIO.equals(a.getUsuario()))
                .count();
        assertEquals(1, encontrados);
    }
}