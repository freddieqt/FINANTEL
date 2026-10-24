package com.finantel.tipo_cambio.sistema;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class CU22TipoCambioSistemaTest {

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
                 "DELETE FROM TipoCambio WHERE fuente = 'SBS' AND CAST(fecha AS date) = CAST(GETDATE() AS date)")) {
            ps.executeUpdate();
        }
    }

    @Test
    void S01_el_administrador_actualiza_el_tipo_de_cambio_desde_la_sbs() {
        page.navigate(BASE + "/login");
        page.fill("input[name='username']", EMAIL);
        page.fill("input[name='password']", CLAVE);
        page.click("button[type='submit']");
        page.waitForURL(url -> !url.contains("/login"));

        APIResponse respuesta = page.request().post(BASE + "/web/tipo-cambio/actualizar");

        assertEquals(200, respuesta.status(), "La actualización del tipo de cambio no respondió 200");
        assertTrue(respuesta.text().contains("compra"), "La respuesta no incluye la tasa de compra");
    }
}