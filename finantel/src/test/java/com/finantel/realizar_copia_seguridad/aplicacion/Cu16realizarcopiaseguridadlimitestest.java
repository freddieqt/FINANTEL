package com.finantel.realizar_copia_seguridad.aplicacion;

import com.finantel.realizar_copia_seguridad.dominio.repository.CopiaSeguridadRepository;
import com.finantel.realizar_copia_seguridad.infraestructura.persistence.CopiaSeguridadConfig;
import com.finantel.registrar_accion.aplicacion.AuditoriaService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CU16RealizarCopiaSeguridadLimitesTest {

    private final CopiaSeguridadConfig config = new CopiaSeguridadConfig();

    @Mock
    private CopiaSeguridadRepository repo;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private RealizarCopiaSeguridadService service;

    @Test
    void CP_L1_rol_nulo_no_tiene_permiso() {
        assertFalse(config.tienePermisos(null));
    }

    @Test
    void CP_L2_rol_vacio_no_tiene_permiso() {
        assertFalse(config.tienePermisos(""));
    }

    @Test
    void CP_L3_sin_copias_listar_devuelve_lista_vacia() {
        when(repo.findAll()).thenReturn(List.of());

        assertTrue(service.listar().isEmpty());
    }
}