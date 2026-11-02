package com.finantel.autenticar_usuario.aplicacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CU02AutenticacionServiceUnitTest {

    private AutenticacionService service;

    @BeforeEach
    void setUp() {
        service = new AutenticacionService();
    }

    @DisplayName("CP-35 · Email y contraseña no vacíos: credenciales válidas")
    @Test
    void cp35_credencialesNoVacias_sonValidas() {
        assertTrue(service.validarCredenciales("prueba@finantel.pe", "clave123"));
    }

    @DisplayName("CP-36 · Email nulo o vacío: credenciales inválidas")
    @ParameterizedTest
    @NullAndEmptySource
    void cp36_emailNuloOVacio_seRechaza(String email) {
        assertFalse(service.validarCredenciales(email, "clave123"));
    }

    @DisplayName("CP-37 · Contraseña nula o vacía: credenciales inválidas")
    @ParameterizedTest
    @NullAndEmptySource
    void cp37_passwordNulaOVacia_seRechaza(String password) {
        assertFalse(service.validarCredenciales("prueba@finantel.pe", password));
    }

    @DisplayName("CP-38 · Email formado solo por espacios: se rechaza (se espera fallo)")
    @Test
    void cp38_emailConSoloEspacios_seRechaza() {
        assertFalse(service.validarCredenciales("   ", "clave123"));
    }

    @DisplayName("CP-39 · Estado Activo sin importar mayúsculas: se considera activo")
    @ParameterizedTest
    @ValueSource(strings = {"Activo", "activo", "ACTIVO"})
    void cp39_estadoActivo_esActivo(String estado) {
        assertTrue(service.estaActivo(estado));
    }

    @DisplayName("CP-40 · Estado distinto de Activo o nulo: no está activo")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"Inactivo", "Activa", "Bloqueado"})
    void cp40_estadoNoActivo_seRechaza(String estado) {
        assertFalse(service.estaActivo(estado));
    }
}