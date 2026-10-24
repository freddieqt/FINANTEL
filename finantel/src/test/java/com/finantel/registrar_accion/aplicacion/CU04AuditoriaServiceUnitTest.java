package com.finantel.registrar_accion.aplicacion;

import com.finantel.registrar_accion.dominio.entity.AuditoriaAccion;
import com.finantel.registrar_accion.dominio.repository.AuditoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CU04AuditoriaServiceUnitTest {

    @Mock private AuditoriaRepository repo;
    private AuditoriaService servicio;

    @BeforeEach
    void setUp() {
        servicio = new AuditoriaService(repo);
    }

    private AuditoriaAccion capturarGuardado() {
        ArgumentCaptor<AuditoriaAccion> captor = ArgumentCaptor.forClass(AuditoriaAccion.class);
        verify(repo).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void registrar_guarda_todos_los_campos() {
        servicio.registrar("admin@finantel.pe", "Crear asiento", "AsientosContables", "-", "AST-2026-001");
        AuditoriaAccion a = capturarGuardado();
        assertEquals("admin@finantel.pe", a.getUsuario());
        assertEquals("Crear asiento", a.getAccion());
        assertEquals("AsientosContables", a.getObjeto());
        assertEquals("-", a.getValorAnterior());
        assertEquals("AST-2026-001", a.getValorNuevo());
    }

    @Test
    void registrar_asigna_fecha_actual() {
        LocalDateTime antes = LocalDateTime.now();
        servicio.registrar("u", "a", "o", null, null);
        LocalDateTime fecha = capturarGuardado().getFecha();
        assertNotNull(fecha);
        assertFalse(fecha.isBefore(antes));
    }

    @Test
    void registrar_acepta_valor_anterior_nulo_en_creacion() {
        servicio.registrar("u", "Crear", "o", null, "nuevo");
        AuditoriaAccion a = capturarGuardado();
        assertNull(a.getValorAnterior());
        assertEquals("nuevo", a.getValorNuevo());
    }

    @Test
    void listar_delega_en_findAll() {
        when(repo.findAll()).thenReturn(List.of(new AuditoriaAccion()));
        assertEquals(1, servicio.listar().size());
        verify(repo).findAll();
    }

    @Test
    void filtrarPorUsuario_delega_en_el_repositorio() {
        when(repo.findByUsuario("u")).thenReturn(List.of());
        assertTrue(servicio.filtrarPorUsuario("u").isEmpty());
        verify(repo).findByUsuario("u");
    }

    @Test
    void filtrarPorAccion_delega_en_el_repositorio() {
        when(repo.findByAccion("Crear")).thenReturn(List.of());
        assertTrue(servicio.filtrarPorAccion("Crear").isEmpty());
        verify(repo).findByAccion("Crear");
    }
}