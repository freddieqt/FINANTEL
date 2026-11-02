package com.finantel.buscar_asiento.aplicacion;

import com.finantel.buscar_asiento.dominio.repository.BuscarAsientoRepository;
import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CU08BuscarAsientoUnitTest {

    @Mock
    private BuscarAsientoRepository repo;

    @InjectMocks
    private BuscarAsientoService service;

    @Test
    void CP_U1_buscarPorTipo_delega_en_el_repositorio_y_devuelve_su_resultado() {
        List<AsientoContable> esperado = List.of(new AsientoContable());
        when(repo.findByTipo("Cierre")).thenReturn(esperado);

        assertSame(esperado, service.buscarPorTipo("Cierre"));
        verify(repo).findByTipo("Cierre");
    }

    @Test
    void CP_U2_buscarPorUsuario_delega_en_el_repositorio() {
        when(repo.findByUsuario("prueba@finantel.pe")).thenReturn(List.of());

        assertTrue(service.buscarPorUsuario("prueba@finantel.pe").isEmpty());
        verify(repo).findByUsuario("prueba@finantel.pe");
    }

    @Test
    void CP_U3_buscarPorEstado_delega_en_el_repositorio() {
        when(repo.findByEstado("Activo")).thenReturn(List.of());

        assertTrue(service.buscarPorEstado("Activo").isEmpty());
        verify(repo).findByEstado("Activo");
    }
}