package com.finantel.buscar_asiento.aplicacion;

import com.finantel.buscar_asiento.dominio.repository.BuscarAsientoRepository;
import com.finantel.buscar_asiento.infraestructura.persistence.BuscarAsientoConfig;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CU08BuscarAsientoLimitesTest {

    private final BuscarAsientoConfig config = new BuscarAsientoConfig();

    @Mock
    private BuscarAsientoRepository repo;

    @InjectMocks
    private BuscarAsientoService service;

    @Test
    void CP_L1_filtro_nulo_es_rechazado() {
        assertFalse(config.esFiltroValido(null));
    }

    @Test
    void CP_L2_filtro_vacio_es_rechazado() {
        assertFalse(config.esFiltroValido(""));
    }

    @Test
    void CP_L3_filtro_solo_con_espacios_es_rechazado() {
        // Comportamiento esperado: "   " no es un filtro válido.
        // Si este CP falla, es un defecto: corregir con filtro.isBlank().
        assertFalse(config.esFiltroValido("   "));
    }

    @Test
    void CP_L4_sin_coincidencias_devuelve_lista_vacia() {
        when(repo.findByTipo("Reversión")).thenReturn(List.of());

        assertTrue(service.buscarPorTipo("Reversión").isEmpty());
    }
}