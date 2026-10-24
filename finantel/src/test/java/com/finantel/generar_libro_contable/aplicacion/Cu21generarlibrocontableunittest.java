package com.finantel.generar_libro_contable.aplicacion;

import com.finantel.generar_libro_contable.dominio.repository.LibroContableRepository;
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
class CU21GenerarLibroContableUnitTest {

    @Mock
    private LibroContableRepository repo;

    @InjectMocks
    private GenerarLibroContableService service;

    @Test
    void CP_U1_generarLibro_devuelve_los_asientos_del_repositorio() {
        when(repo.findAll()).thenReturn(List.of(new AsientoContable(), new AsientoContable()));

        List<AsientoContable> libro = service.generarLibro();

        assertEquals(2, libro.size());
        verify(repo).findAll();
    }

    @Test
    void CP_U2_sin_asientos_el_libro_queda_vacio() {
        when(repo.findAll()).thenReturn(List.of());

        assertTrue(service.generarLibro().isEmpty());
    }
}