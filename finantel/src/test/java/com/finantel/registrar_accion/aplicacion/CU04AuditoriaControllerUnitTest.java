package com.finantel.registrar_accion.aplicacion;

import com.finantel.registrar_accion.presentacion.AuditoriaController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CU04AuditoriaControllerUnitTest {

    @Mock private AuditoriaService service;
    private AuditoriaController controller;

    @BeforeEach
    void setUp() {
        controller = new AuditoriaController(service);
    }

    @Test
    void sin_filtros_llama_a_listar() {
        when(service.listar()).thenReturn(List.of());
        assertTrue(controller.listar(null, null).isEmpty());
        verify(service).listar();
    }

    @Test
    void con_usuario_llama_a_filtrarPorUsuario() {
        when(service.filtrarPorUsuario("u")).thenReturn(List.of());
        controller.listar("u", null);
        verify(service).filtrarPorUsuario("u");
    }

    @Test
    void con_accion_llama_a_filtrarPorAccion() {
        when(service.filtrarPorAccion("Crear")).thenReturn(List.of());
        controller.listar(null, "Crear");
        verify(service).filtrarPorAccion("Crear");
    }

    @Test
    void usuario_vacio_cae_al_filtro_por_accion() {
        when(service.filtrarPorAccion("Crear")).thenReturn(List.of());
        controller.listar("", "Crear");
        verify(service).filtrarPorAccion("Crear");
        verify(service, never()).filtrarPorUsuario(anyString());
    }
}