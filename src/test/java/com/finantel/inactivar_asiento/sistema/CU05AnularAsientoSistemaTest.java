
package com.finantel.inactivar_asiento.sistema;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CU05AnularAsientoSistemaTest {

    private static final String BASE = "http://localhost:8080";
    private static final String URL_BD =
        "jdbc:sqlserver://localhost:1433;databaseName=FINANTEL_TEST;encrypt=false;trustServerCertificate=true";
    private static final String EMAIL = "prueba@finantel.pe";
    private static final String CLAVE = "clave123";

    private static Playwright playwright;
    private static Browser browser;

    private String correlativo;
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
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    private static Connection conexion() throws SQLException {
        return DriverManager.getConnection(URL_BD, "sa", "admin098");
    }

    @BeforeEach
    void preparar() throws SQLException {
        correlativo = "CU05S-" + (System.nanoTime() % 100000);

        String sql = """
            INSERT INTO AsientosContables
                (descripcion, debe, haber, tipo, estado, usuario,
                 cuenta_contable, centro_costo, moneda, tipo_cambio,
                 periodo_id, fecha_registro, numero_correlativo)
            VALUES
                ('CU05 sistema', 100, 100, 'Operación ordinaria',
                 'Activo', ?, '1011', 'General', 'PEN', 1.0,
                 1, GETDATE(), ?)
            """;

        try (Connection c = conexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, EMAIL);
            ps.setString(2, correlativo);
            ps.executeUpdate();
        }

        page = browser.newPage();
    }

    @AfterEach
    void limpiar() throws SQLException {
        if (page != null) {
            page.close();
        }

        try (Connection c = conexion();
             PreparedStatement ps = c.prepareStatement(
                 "DELETE FROM AsientosContables WHERE numero_correlativo = ?"
             )) {
            ps.setString(1, correlativo);
            ps.executeUpdate();
        }
    }

    private String estadoEnBd() throws SQLException {
        try (Connection c = conexion();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT estado FROM AsientosContables WHERE numero_correlativo = ?"
             )) {
            ps.setString(1, correlativo);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new AssertionError(
                        "No se encontró el asiento: " + correlativo
                    );
                }
                return rs.getString("estado");
            }
        }
    }

    @Test
    void S01_anular_desde_la_pantalla_marca_el_asiento_como_anulado()
            throws SQLException {

        // Iniciar sesión
        page.navigate(BASE + "/login");
        page.fill("input[name='username']", EMAIL);
        page.fill("input[name='password']", CLAVE);
        page.click("button[type='submit']");
        page.waitForURL(url -> !url.contains("/login"));

        // Abrir el panel de asientos
        page.navigate(BASE + "/dashboard");
        page.evaluate("showPanel('asientos')");

        // Diagnóstico de errores del navegador y respuestas HTTP
        page.onConsoleMessage(msg ->
            System.out.println("CONSOLA NAVEGADOR: " + msg.text())
        );

        page.onPageError(error ->
            System.out.println("ERROR JAVASCRIPT: " + error)
        );

        page.onResponse(response -> {
            if (response.status() >= 400
                    || response.url().contains("/web/asientos")) {
                System.out.println(
                    "HTTP " + response.status()
                    + " | " + response.request().method()
                    + " | " + response.url()
                );
            }
        });

        page.onRequestFailed(request ->
            System.out.println(
                "PETICIÓN FALLIDA: " + request.url()
                + " | " + request.failure()
            )
        );

        // Abrir el modal de anulación
        page.locator("tr",
                new Page.LocatorOptions().setHasText(correlativo))
            .locator("button[title='Anular']")
            .click();

        // Introducir el motivo
        page.locator("#motivo-anular")
            .fill("Prueba automatizada CU05");

        // Confirmar y esperar la petición al servidor
        page.waitForResponse(
            response ->
                response.url().contains("/web/asientos/anular/")
                    && response.request().method().equals("PATCH"),
            () -> page.locator("#modal-anular .btn-danger").click()
        );

        // Verificar el estado persistido en SQL Server
        String estadoReal = estadoEnBd();
        System.out.println("ESTADO REAL DEL ASIENTO: [" + estadoReal + "]");

        assertEquals("Anulado", estadoReal);
    }
}
