package com.finantel.clasificar_asiento.aplicacion;

import com.finantel.clasificar_asiento.infraestructura.mapping.ClasificarAsientoMapper;
import com.finantel.clasificar_asiento.infraestructura.persistence.ClasificarAsientoConfig;
import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import com.finantel.registrar_asiento_contable.dominio.repository.AsientoContableRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CU07ClasificarAsientoLimitesTest {

    @Mock
    private AsientoContableRepository repo;

    @InjectMocks
    private ClasificarAsientoService service;

    @Test
    void CP_L1_tipo_vacio_no_devuelve_asientos() {
        AsientoContable a = new AsientoContable();
        a.setTipo("Cierre");
        when(repo.findAll()).thenReturn(List.of(a));

        assertTrue(service.listarPorTipo("").isEmpty());
    }

    @Test
    void CP_L2_sin_asientos_en_la_bd_devuelve_lista_vacia() {
        when(repo.findAll()).thenReturn(List.of());

        assertTrue(service.listarPorTipo("Cierre").isEmpty());
    }

    @Test
    void CP_L3_tipo_nulo_en_null_es_rechazado() {
        // Espera comportamiento correcto: null no es un tipo válido.
        // Si el método lanza NullPointerException, este CP falla y es un defecto a corregir.
        ClasificarAsientoConfig config = new ClasificarAsientoConfig();

        assertFalse(config.esTipoValido(null));
    }

    @Test
    void CP_L4_mapper_con_null_devuelve_SIN_TIPO() {
        assertEquals("SIN_TIPO", new ClasificarAsientoMapper().toTipo(null));
    }

    @Test
    void CP_L5_mapper_quita_espacios_y_pasa_a_mayusculas() {
        assertEquals("CIERRE", new ClasificarAsientoMapper().toTipo("  cierre  "));
    }
}