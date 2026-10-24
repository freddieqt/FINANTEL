package com.finantel.clasificar_asiento.aplicacion;

import com.finantel.clasificar_asiento.infraestructura.persistence.ClasificarAsientoConfig;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class CU07ClasificarAsientoEquivalenciaTest {

    private final ClasificarAsientoConfig config = new ClasificarAsientoConfig();

    @ParameterizedTest
    @ValueSource(strings = {"Apertura", "Operación ordinaria", "Ajuste", "Cierre", "Reversión"})
    void CP_E1_tipos_validos_son_aceptados(String tipo) {
        assertTrue(config.esTipoValido(tipo));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Otro", "Ingresos", "Consolidación"})
    void CP_E2_tipos_inexistentes_son_rechazados(String tipo) {
        assertFalse(config.esTipoValido(tipo));
    }

    @Test
    void CP_E3_la_validacion_distingue_mayusculas() {
        // Documenta el comportamiento actual: "cierre" en minúscula no es un tipo válido
        assertFalse(config.esTipoValido("cierre"));
    }

    @Test
    void CP_E4_todos_los_tipos_que_devuelve_el_servicio_son_validos() {
        ClasificarAsientoService service = new ClasificarAsientoService(null);

        service.getTiposDisponibles().forEach(tipo ->
            assertTrue(config.esTipoValido(tipo), "Tipo no válido: " + tipo));
    }
}