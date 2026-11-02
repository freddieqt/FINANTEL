package com.finantel.registrar_asiento_contable.dominio.entity;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CalculoCuadreTest {

    @Test
    void cp01_cuadreExacto() {
        List<Partida> p = Arrays.asList(
            new Partida("1011", 100.0, null),
            new Partida("3011", null, 100.0));
        assertEquals(100.0, CalculoCuadre.totalDebe(p), 0.001);
        assertEquals(100.0, CalculoCuadre.totalHaber(p), 0.001);
        assertEquals(0.0, CalculoCuadre.diferencia(p), 0.001);
        assertTrue(CalculoCuadre.estaCuadrado(p));
        assertEquals("Cuadrado", CalculoCuadre.getEstadoCuadre(p));
    }

    @Test
    void cp02_descuadre() {
        List<Partida> p = Arrays.asList(
            new Partida("1011", 100.0, null),
            new Partida("3011", null, 80.0));
        assertEquals(20.0, CalculoCuadre.diferencia(p), 0.001);
        assertFalse(CalculoCuadre.estaCuadrado(p));
        assertEquals("Descuadrado", CalculoCuadre.getEstadoCuadre(p));
    }

    @Test
    void cp03_variasPartidasDelMismoLado() {
        List<Partida> p = Arrays.asList(
            new Partida("1011", 60.0, null),
            new Partida("1201", 40.0, null),
            new Partida("3011", null, 100.0));
        assertEquals(100.0, CalculoCuadre.totalDebe(p), 0.001);
        assertTrue(CalculoCuadre.estaCuadrado(p));
    }

    @Test
    void cp04_decimales() {
        List<Partida> p = Arrays.asList(
            new Partida("A", 0.1, null),
            new Partida("B", 0.2, null),
            new Partida("C", null, 0.3));
        assertEquals(0.0, CalculoCuadre.diferencia(p), 0.001);
        assertTrue(CalculoCuadre.estaCuadrado(p));
    }

    @Test
    void cp05_toleranciaDeUnCentimo() {
        List<Partida> p = Arrays.asList(
            new Partida("1011", 100.00, null),
            new Partida("3011", null, 99.995));
        assertTrue(CalculoCuadre.estaCuadrado(p));
    }

    @Test
    void cp06_ignoraNulos() {
        List<Partida> p = Arrays.asList(
            new Partida("1011", null, 50.0),
            new Partida("3011", 50.0, null));
        assertDoesNotThrow(() -> CalculoCuadre.estaCuadrado(p));
        assertEquals(50.0, CalculoCuadre.totalDebe(p), 0.001);
        assertEquals(50.0, CalculoCuadre.totalHaber(p), 0.001);
    }

    @Test
    void cp07_listaVaciaONull() {
        assertEquals(0.0, CalculoCuadre.totalDebe(null), 0.001);
        assertEquals(0.0, CalculoCuadre.totalDebe(List.of()), 0.001);
        assertFalse(CalculoCuadre.estaCuadrado(null));
        assertFalse(CalculoCuadre.estaCuadrado(List.of()));
    }
}