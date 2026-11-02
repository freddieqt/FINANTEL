package com.finantel.validar_cuentas.sistema;

import com.finantel.pantalla.PantallaBase;
import org.junit.jupiter.api.Test;

public class ValidarCuentasSistemaTest extends PantallaBase {

    private void abrirNuevaCuenta() {
        iniciarComoContador();
        page.evaluate("nuevaCuenta()");
        page.waitForSelector("#nc-cod");
    }

    private void guardarCuenta() {
        page.click("button[onclick='confirmarNuevaCuenta()']");
    }

    // ENTRADA: [CU10 pantalla] código = "" (vacío); descripción = "Cuentas por cobrar"
    // RESULTADO: Se muestra el mensaje "El código de cuenta es obligatorio"
    @Test
    void codigo_vacio_muestra_error() {
        abrirNuevaCuenta();
        page.fill("#nc-cod", "");
        page.fill("#nc-des", "Cuentas por cobrar");
        guardarCuenta();
        assertToastContiene("El código de cuenta es obligatorio");
    }

    // ENTRADA: [CU10 pantalla] código = 1011; descripción = "" (vacía)
    // RESULTADO: Se muestra el mensaje "La descripción es obligatoria"
    @Test
    void descripcion_vacia_muestra_error() {
        abrirNuevaCuenta();
        page.fill("#nc-cod", "1011");
        page.fill("#nc-des", "");
        guardarCuenta();
        assertToastContiene("La descripción es obligatoria");
    }

    // ENTRADA: [CU10 pantalla] código = 1011; descripción = "Cuentas por cobrar"; clasificación = "" (sin seleccionar)
    // RESULTADO: Se muestra el mensaje "Selecciona la clasificación"
    @Test
    void clasificacion_sin_seleccionar_muestra_error() {
        abrirNuevaCuenta();
        page.fill("#nc-cod", "1011");
        page.fill("#nc-des", "Cuentas por cobrar");
        dejarSinSeleccionar("nc-cla");
        guardarCuenta();
        assertToastContiene("Selecciona la clasificación");
    }

    // ENTRADA: [CU10 pantalla] código = 1011; descripción = "Cuentas por cobrar"; clasificación = válida; tipo = ninguno marcado
    // RESULTADO: Se muestra el mensaje "Selecciona el tipo (Débito o Crédito)"
    @Test
    void tipo_sin_marcar_muestra_error() {
        abrirNuevaCuenta();
        page.fill("#nc-cod", "1011");
        page.fill("#nc-des", "Cuentas por cobrar");
        elegirPrimeraOpcionValida("nc-cla");
        page.evaluate("() => document.querySelectorAll(\"input[name=nc-tip]\").forEach(r => r.checked = false)");
        guardarCuenta();
        assertToastContiene("Selecciona el tipo (Débito o Crédito)");
    }
}
