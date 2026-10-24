package com.finantel.adjuntar_documento.sistema;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.SelectOption;
import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class AdjuntarDocumentoSistemaTest {

    private static final String BASE = "http://localhost:8080";
    private static final String EMAIL = "admin.prueba@finantel.pe";
    private static final String CLAVE = "clave123";
    private static final String URL_BD =
        "jdbc:sqlserver://localhost:1433;databaseName=FINANTEL_TEST;encrypt=false;trustServerCertificate=true";
    private static final byte[] PDF_BYTES = {0x25, 0x50, 0x44, 0x46, 0x2D, 0x31, 0x2E, 0x34};

    private static Playwright playwright;
    private static Browser browser;
    private Page page;

    @BeforeAll
    static void iniciar() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
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
        try (Connection c = DriverManager.getConnection(URL_BD, "sa", "admin098")) {
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM DocumentosAsiento WHERE usuario = ?")) {
                ps.setString(1, EMAIL);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "DELETE FROM AsientosContables WHERE usuario = ? AND CAST(fecha_registro AS date) = CAST(GETDATE() AS date)")) {
                ps.setString(1, EMAIL);
                ps.executeUpdate();
            }
        }
    }

    @Test
    void S01_el_usuario_adjunta_un_documento_al_registrar_un_asiento() throws Exception {
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
        page.fill("#asiento-ref-comprobante", "F001-DOC-" + (System.currentTimeMillis() % 100000));
        page.fill("#asiento-fecha-contable", "2026-10-23");

        Locator linea1 = page.locator("#asiento-lineas .asiento-line").nth(0);
        Locator linea2 = page.locator("#asiento-lineas .asiento-line").nth(1);
        linea1.locator("select").selectOption(new SelectOption().setIndex(1));
        linea2.locator("select").selectOption(new SelectOption().setIndex(1));
        linea1.locator("input[type=number]").nth(0).fill("500");
        linea2.locator("input[type=number]").nth(1).fill("500");

        Path pdf = Files.createTempFile("respaldo", ".pdf");
        Files.write(pdf, PDF_BYTES);
        page.setInputFiles("#asiento-documento", pdf);

        Response respuesta = page.waitForResponse(
            r -> r.url().contains("/documento") && "POST".equals(r.request().method()),
            () -> page.click("button[onclick='guardarAsiento()']")
        );

        assertEquals(200, respuesta.status(), "El documento no se adjuntó desde el formulario");
    }
}