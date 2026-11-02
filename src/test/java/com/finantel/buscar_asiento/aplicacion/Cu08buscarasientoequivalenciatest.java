package com.finantel.buscar_asiento.aplicacion;

import com.finantel.buscar_asiento.infraestructura.mapping.BuscarAsientoMapper;
import com.finantel.buscar_asiento.infraestructura.persistence.BuscarAsientoConfig;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class CU08BuscarAsientoEquivalenciaTest {

    private final BuscarAsientoConfig config = new BuscarAsientoConfig();
    private final BuscarAsientoMapper mapper = new BuscarAsientoMapper();

    @ParameterizedTest
    @ValueSource(strings = {"Cierre", "Activo", "prueba@finantel.pe", "Operación ordinaria"})
    void CP_E1_filtros_con_contenido_son_validos(String filtro) {
        assertTrue(config.esFiltroValido(filtro));
    }

    @ParameterizedTest
    @CsvSource({
        "'  CIERRE  ', cierre",
        "'Activo', activo",
        "'Cierre', cierre"
    })
    void CP_E2_el_mapper_normaliza_recortando_y_pasando_a_minusculas(String entrada, String esperado) {
        assertEquals(esperado, mapper.normalizarFiltro(entrada));
    }

    @Test
    void CP_E3_el_mapper_convierte_null_en_cadena_vacia() {
        assertEquals("", mapper.normalizarFiltro(null));
    }
}