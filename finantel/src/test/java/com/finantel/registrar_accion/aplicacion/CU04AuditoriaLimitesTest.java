package com.finantel.registrar_accion.aplicacion;

import com.finantel.registrar_accion.dominio.repository.AuditoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CU04AuditoriaLimitesTest {

    @Mock private AuditoriaRepository repo;
    private AuditoriaService servicio;

    @BeforeEach
    void setUp() {
        servicio = new AuditoriaService(repo);
    }

    @Test
    void listar_sin_registros_devuelve_lista_vacia() {
        when(repo.findAll()).thenReturn(List.of());
        assertTrue(servicio.listar().isEmpty());
    }

    @Test
    void filtrarPorUsuario_inexistente_devuelve_lista_vacia() {
        when(repo.findByUsuario("nadie@finantel.pe")).thenReturn(List.of());
        assertTrue(servicio.filtrarPorUsuario("nadie@finantel.pe").isEmpty());
    }

    @Test
    void filtrarPorAccion_inexistente_devuelve_lista_vacia() {
        when(repo.findByAccion("Accion inexistente")).thenReturn(List.of());
        assertTrue(servicio.filtrarPorAccion("Accion inexistente").isEmpty());
    }
}