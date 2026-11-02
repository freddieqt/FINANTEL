package com.finantel.registrar_asiento_contable.sistema;

import com.finantel.pantalla.PantallaBase;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.SelectOption;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ValidacionesAsientoPantallaSistemaTest extends PantallaBase {

    private static final String EMAIL = "admin.prueba@finantel.pe";
    private static final String URL_BD =
        "jdbc:sqlserver://localhost:1433;databaseName=FINANTEL_TEST;encrypt=false;trustServerCertificate=true";

    @AfterEach
    void limpiar() throws SQLException {
        try (Connection c = DriverManager.getConnection(URL_BD, "sa", "admin098");
             PreparedStatement ps = c.prepareStatement(
                 "DELETE FROM AsientosContables WHERE usuario = ? AND CAST(fecha_registro AS date) = CAST(GETDATE() AS date)")) {
            ps.setString(1, EMAIL);
            ps.executeUpdate();
        }
    }

    private void abrirNuevoAsiento() {
        iniciarComoContador(); // Método heredado de PantallaBase
        page.evaluate("openNuevoAsiento()");
        page.waitForFunction("document.getElementById('periodoId').options.length > 1");
        page.evaluate("const p = document.getElementById('periodoId'); p.selectedIndex = 1; p.dispatchEvent(new Event('change'));");
    }

    private void guardar() {
        page.click("text=💾 Guardar Asiento");
    }

    @Test
    void S01_el_usuario_registra_un_asiento_desde_la_pantalla() {
        abrirNuevoAsiento();
        page.selectOption("#asiento-tipo", new SelectOption().setIndex(1));
        page.selectOption("#asiento-tipo-comprobante", new SelectOption().setIndex(1));
        page.fill("#asiento-ref-comprobante", "F001-SIS-" + (System.currentTimeMillis() % 100000));
        page.fill("#asiento-fecha-contable", "2026-10-23");
        
        Locator linea1 = page.locator("#asiento-lineas .asiento-line").nth(0);
        Locator linea2 = page.locator("#asiento-lineas .asiento-line").nth(1);
        linea1.locator("select").selectOption(new SelectOption().setIndex(1));
        linea2.locator("select").selectOption(new SelectOption().setIndex(1));
        linea1.locator("input[type=number]").nth(0).fill("500");
        linea2.locator("input[type=number]").nth(1).fill("500");

        Response respuesta = page.waitForResponse(
            r -> r.url().contains("/web/asientos") && "POST".equals(r.request().method()),
            () -> page.click("button:has-text('💾 Guardar')")
        );

        assertEquals(200, respuesta.status(), "Status: " + respuesta.status());
    }

    // ENTRADA: [CU01 pantalla] tipo = "" (sin seleccionar); comprobante = F; referencia = 001-00123
    // RESULTADO: Se muestra el mensaje "Tipo de asiento es obligatorio"
    @Test
    void tipo_vacio_muestra_error() {
        abrirNuevoAsiento();
        page.selectOption("#asiento-tipo-comprobante", "F");
        page.fill("#asiento-ref-comprobante", "001-00123");
        dejarSinSeleccionar("asiento-tipo"); // Método heredado de PantallaBase
        guardar();
        assertToastContiene("Tipo de asiento es obligatorio"); // Método heredado de PantallaBase
    }

    // ENTRADA: [CU01 pantalla] tipo-comprobante = "" (sin seleccionar); referencia = 001-00123
    // RESULTADO: Se muestra el mensaje "Selecciona tipo de comprobante"
    @Test
    void comprobante_sin_seleccionar_muestra_error() {
        abrirNuevoAsiento();
        dejarSinSeleccionar("asiento-tipo-comprobante");
        page.fill("#asiento-ref-comprobante", "001-00123");
        guardar();
        assertToastContiene("Selecciona tipo de comprobante");
    }

    // ENTRADA: [CU01 pantalla] tipo-comprobante = F; referencia = "" (vacía)
    // RESULTADO: Se muestra el mensaje "Ingresa número de comprobante"
    @Test
    void numero_de_comprobante_vacio_muestra_error() {
        abrirNuevoAsiento();
        page.selectOption("#asiento-tipo-comprobante", "F");
        page.fill("#asiento-ref-comprobante", "");
        guardar();
        assertToastContiene("Ingresa número de comprobante");
    }

    // ENTRADA: [CU01 pantalla] tipo = "Operación ordinaria" o "Reversión"; comprobante = F; referencia = 001-00123
    // RESULTADO: No se muestra ningún error de tipo de asiento
    @Test
    void tipos_operacion_ordinaria_y_reversion_pasan_la_validacion() {
        for (String tipo : new String[]{"Operación ordinaria", "Reversión"}) {
            abrirNuevoAsiento();
            page.selectOption("#asiento-tipo-comprobante", "F");
            page.fill("#asiento-ref-comprobante", "001-00123");
            page.selectOption("#asiento-tipo", new SelectOption().setLabel(tipo));
            guardar();
            assertNoToastContiene("Tipo de asiento es obligatorio"); // Método heredado de PantallaBase
            page.reload();
        }
    }

    // ENTRADA: [CU01 pantalla] tipo-comprobante = F, B, R, ND o NC; referencia = 001-00123
    // RESULTADO: No se muestra el error "Selecciona tipo de comprobante"
    @Test
    void comprobantes_F_B_R_ND_NC_pasan_la_validacion() {
        for (String codigo : new String[]{"F", "B", "R", "ND", "NC"}) {
            abrirNuevoAsiento();
            page.selectOption("#asiento-tipo-comprobante", codigo);
            page.fill("#asiento-ref-comprobante", "001-00123");
            guardar();
            assertNoToastContiene("Selecciona tipo de comprobante");
            page.reload();
        }
    }

    // ENTRADA: [CU01 pantalla] tipo-comprobante = F; referencia = 001-00123
    // RESULTADO: No se muestra el error "Ingresa número de comprobante"
    @Test
    void numero_de_comprobante_con_formato_valido_pasa_la_validacion() {
        abrirNuevoAsiento();
        page.selectOption("#asiento-tipo-comprobante", "F");
        page.fill("#asiento-ref-comprobante", "001-00123");
        guardar();
        assertNoToastContiene("Ingresa número de comprobante");
    }
}