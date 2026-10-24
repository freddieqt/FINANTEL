package com.finantel.registrar_asiento_contable.sistema;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.SelectOption;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class CU01RegistrarAsientoSistemaTest {

    private static final String BASE = "http://localhost:8080";
    private static final String EMAIL = "admin.prueba@finantel.pe";
    private static final String CLAVE = "clave123";
    private static final String URL_BD =
        "jdbc:sqlserver://localhost:1433;databaseName=FINANTEL_TEST;encrypt=false;trustServerCertificate=true";

    private static Playwright playwright;
    private static Browser browser;
    private Page page;

    @BeforeAll
    static void iniciar() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
            new BrowserType.LaunchOptions().setHeadless(false)
        );
    }

    @AfterAll
    static void cerrar() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @BeforeEach
    void preparar() {
        page = browser.newPage();
    }

    @AfterEach
    void limpiar() throws SQLException {
        if (page != null) page.close();
        try (Connection c = DriverManager.getConnection(URL_BD, "sa", "admin098");
             PreparedStatement ps = c.prepareStatement(
                 "DELETE FROM AsientosContables WHERE usuario = ? AND CAST(fecha_registro AS date) = CAST(GETDATE() AS date)")) {
            ps.setString(1, EMAIL);
            ps.executeUpdate();
        }
    }

    @Test
    void S01_el_usuario_registra_un_asiento_desde_la_pantalla() {
        page.navigate(BASE + "/login");
        page.fill("input[name='username']", EMAIL);
        page.fill("input[name='password']", CLAVE);
        page.click("button[type='submit']");
        page.waitForURL(url -> !url.contains("/login"));

        page.navigate(BASE + "/dashboard");
        page.evaluate("openNuevoAsiento()");
        page.waitForFunction("document.getElementById('periodoId').options.length > 1");
        page.evaluate("const p = document.getElementById('periodoId'); p.selectedIndex = 1; p.dispatchEvent(new Event('change'));");
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
}