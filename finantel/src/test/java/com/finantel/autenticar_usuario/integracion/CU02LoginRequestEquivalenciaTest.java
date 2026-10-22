package com.finantel.autenticar_usuario.integracion;

import com.finantel.autenticar_usuario.aplicacion.LoginRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CU02LoginRequestEquivalenciaTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void iniciar() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void cerrar() {
        factory.close();
    }

    private Set<ConstraintViolation<LoginRequest>> validar(String email, String password) {
        LoginRequest r = new LoginRequest();
        r.setEmail(email);
        r.setPassword(password);
        return validator.validate(r);
    }

    private Set<String> mensajes(Set<ConstraintViolation<LoginRequest>> violaciones) {
        return violaciones.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
    }

    @DisplayName("CP-41 · Email y contraseña válidos: no hay errores")
    @Test
    void cp41_datosValidos_sinErrores() {
        assertTrue(validar("prueba@finantel.pe", "123456").isEmpty());
    }

    @DisplayName("CP-42 · Email nulo o vacío: se rechaza con mensaje de obligatorio")
    @ParameterizedTest
    @NullAndEmptySource
    void cp42_emailNuloOVacio_seRechaza(String email) {
        assertTrue(mensajes(validar(email, "123456")).contains("El email es obligatorio"));
    }

    @DisplayName("CP-43 · Email de otro dominio: se rechaza")
    @ParameterizedTest
    @ValueSource(strings = {"prueba@gmail.com", "prueba@finantel.com"})
    void cp43_emailOtroDominio_seRechaza(String email) {
        assertTrue(mensajes(validar(email, "123456"))
            .contains("Ingrese un correo válido de FINANTEL (@finantel.pe)"));
    }

    @DisplayName("CP-44 · Email con dominio que empieza igual pero termina distinto: se rechaza")
    @Test
    void cp44_dominioExtendido_seRechaza() {
        assertTrue(mensajes(validar("prueba@finantel.pe.com", "123456"))
            .contains("Ingrese un correo válido de FINANTEL (@finantel.pe)"));
    }

    @DisplayName("CP-45 · Contraseña de 5 caracteres (límite inferior inválido): se rechaza")
    @Test
    void cp45_passwordDeCincoCaracteres_seRechaza() {
        assertTrue(mensajes(validar("prueba@finantel.pe", "12345"))
            .contains("La contraseña debe tener mínimo 6 caracteres"));
    }

    @DisplayName("CP-46 · Contraseña de 6 caracteres (límite inferior válido): se acepta")
    @Test
    void cp46_passwordDeSeisCaracteres_seAcepta() {
        assertTrue(validar("prueba@finantel.pe", "123456").isEmpty());
    }

    @DisplayName("CP-47 · Contraseña nula o vacía: se rechaza con mensaje de obligatoria")
    @ParameterizedTest
    @NullAndEmptySource
    void cp47_passwordNulaOVacia_seRechaza(String password) {
        assertTrue(mensajes(validar("prueba@finantel.pe", password))
            .contains("La contraseña es obligatoria"));
    }
}