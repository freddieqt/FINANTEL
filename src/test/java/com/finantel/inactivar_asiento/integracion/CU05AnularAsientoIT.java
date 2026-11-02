package com.finantel.inactivar_asiento.integracion;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class CU05AnularAsientoSistemaTest {

    private static final String BASE = "http://localhost:8080";
    private static final String URL_BD = "jdbc:sqlserver://localhost:1433;databaseName=FINANTEL_TEST;encrypt=false;trustServerCertificate=true";
    private static final String EMAIL = "prueba@finantel.pe";
    private static final String CLAVE = "clave123";

    private static Playwright playwright;
    private static Browser browser;
    private String correlativo;
    private Page page;

    @BeforeAll
    static void iniciar() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
    }

    @AfterAll
    static void cerrar() {
        browser.close();
        playwright.close();
    }

    private static Connection conexion() throws SQLException {
        return DriverManager.getConnection(URL_BD, "sa", "admin098");
    }

    @BeforeEach
    void preparar() throws SQLException {
        correlativo = "CU05S-" + (System.nanoTime() % 100000);
        try (Connection c = conexion();
             PreparedStatement ps = c.prepareStatement(
                 "INSERT INTO AsientosContables (descripcion, debe, haber, tipo, estado, usuario, cuenta_contable, centro_costo, moneda, tipo_cambio, periodo_id, fecha_registro, numero_correlativo) " +
                 "VALUES ('CU05 sistema', 100, 100, 'Operaci\u00f3n ordinaria', 'Activo', ?, '1011', 'General', 'PEN', 1.0, 1, GETDATE(), ?)")) {
            ps.setString(1, EMAIL);
            ps.setString(2, correlativo);
            ps.executeUpdate();
        }
        page = browser.newPage();
    }

    @AfterEach
    void limpiar() throws SQLException {
        page.close();
        try (Connection c = conexion();
             PreparedStatement ps = c.prepareStatement("DELETE FROM AsientosContables WHERE numero_correlativo = ?")) {
            ps.setString(1, correlativo);
            ps.executeUpdate();
        }
    }

    private String estadoEnBd() throws SQLException {
        try (Connection c = conexion();
             PreparedStatement ps = c.prepareStatement("SELECT estado FROM AsientosContables WHERE numero_correlativo = ?")) {
            ps.setString(1, correlativo);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getString(1);
            }
        }
    }

    @Test
    void S01_anular_desde_la_pantalla_marca_el_asiento_como_anulado() throws SQLException {
        page.navigate(BASE + "/login");
        page.fill("input[name='username']", EMAIL);
        page.fill("input[name='password']", CLAVE);
        page.click("button[type='submit']");
        page.waitForURL(url -> !url.contains("/login"));

        page.navigate(BASE + "/dashboard");
        page.evaluate("showPanel('asientos')");
        page.onDialog(d -> d.accept());

        // Apertura del modal, llenado del motivo y confirmación
        page.locator("tr", new Page.LocatorOptions().setHasText(correlativo))
            .locator("button[title='Anular']").click();
        page.fill("#motivo-anular", "Prueba CU05");
        page.click("#modal-anular button.btn-danger");

        page.waitForTimeout(2000);
        assertEquals("Anulado", estadoEnBd());
    }
}