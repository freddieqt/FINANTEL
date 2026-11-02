package com.finantel.gestionar_periodos.sistema;

import com.finantel.pantalla.PantallaBase;
import org.junit.jupiter.api.Test;

public class ValidacionesPeriodoPantallaSistemaTest extends PantallaBase {

    private void abrirNuevoPeriodo() {
        iniciarComoContador();
        page.evaluate("nuevoPeriodo()");
        page.waitForSelector("#np-nom");
    }

    private void crear() {
        page.click("button[onclick='confirmarNuevoPeriodo()']");
    }

    // ENTRADA: [Gestionar periodos pantalla] nombre = "" (vacío); inicio = 2026-11-01; cierre = 2026-11-30
    // RESULTADO: Se muestra el mensaje "El nombre del periodo es obligatorio"
    @Test
    void nombre_vacio_muestra_error() {
        abrirNuevoPeriodo();
        page.fill("#np-nom", "");
        page.fill("#np-ini", "2026-11-01");
        page.fill("#np-cie", "2026-11-30");
        crear();
        assertToastContiene("El nombre del periodo es obligatorio");
    }

    // ENTRADA: [Gestionar periodos pantalla] nombre = 2026-11; inicio = "" (vacío); cierre = 2026-11-30
    // RESULTADO: Se muestra el mensaje "La fecha de inicio es obligatoria"
    @Test
    void fecha_inicio_vacia_muestra_error() {
        abrirNuevoPeriodo();
        page.fill("#np-nom", "2026-11");
        page.fill("#np-ini", "");
        page.fill("#np-cie", "2026-11-30");
        crear();
        assertToastContiene("La fecha de inicio es obligatoria");
    }

    // ENTRADA: [Gestionar periodos pantalla] nombre = 2026-11; inicio = 2026-11-01; cierre = "" (vacía)
    // RESULTADO: Se muestra el mensaje "La fecha de cierre es obligatoria"
    @Test
    void fecha_cierre_vacia_muestra_error() {
        abrirNuevoPeriodo();
        page.fill("#np-nom", "2026-11");
        page.fill("#np-ini", "2026-11-01");
        page.fill("#np-cie", "");
        crear();
        assertToastContiene("La fecha de cierre es obligatoria");
    }

    // ENTRADA: [Gestionar periodos pantalla] nombre = 2026-11; inicio = 2026-11-30; cierre = 2026-11-01 (anterior al inicio)
    // RESULTADO: Se muestra el mensaje "El cierre debe ser posterior al inicio"
    @Test
    void cierre_anterior_al_inicio_muestra_error() {
        abrirNuevoPeriodo();
        page.fill("#np-nom", "2026-11");
        page.fill("#np-ini", "2026-11-30");
        page.fill("#np-cie", "2026-11-01");
        crear();
        assertToastContiene("El cierre debe ser posterior al inicio");
    }
}
