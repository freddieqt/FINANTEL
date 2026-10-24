package com.finantel.duplicar_asiento.sistema;

import com.finantel.registrar_asiento_contable.aplicacion.AsientoContableService;
import com.finantel.registrar_asiento_contable.dominio.repository.AsientoContableRepository;
import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import com.finantel.registrar_accion.aplicacion.AuditoriaService;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class CU06DuplicarAsientoSistemaTest {

    private static final String BASE = "http://localhost:8080";
    private static final String URL_BD = "jdbc:sqlserver://localhost:1433;databaseName=FINANTEL_TEST;encrypt=false;trustServerCertificate=true";
    private static final String EMAIL = "prueba@finantel.pe";
    private static final String CLAVE = "clave123";

    private static Playwright playwright;
    private static Browser browser;
    private String etiqueta;
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
        etiqueta = "CU06S" + (System.nanoTime() % 100000);
        try (Connection c = conexion();
             PreparedStatement ps = c.prepareStatement(
                 "INSERT INTO AsientosContables (descripcion, debe, haber, tipo, estado, usuario, cuenta_contable, centro_costo, moneda, tipo_cambio, periodo_id, fecha_registro, numero_correlativo) " +
                 "VALUES (?, 100, 100, 'Operaci\u00f3n ordinaria', 'Activo', ?, '1011', 'General', 'PEN', 1.0, 1, GETDATE(), ?)")) {
            ps.setString(1, etiqueta + " prueba");
            ps.setString(2, EMAIL);
            ps.setString(3, etiqueta);
            ps.executeUpdate();
        }
        page = browser.newPage();
    }

    @AfterEach
    void limpiar() throws SQLException {
        page.close();
        try (Connection c = conexion();
             PreparedStatement ps = c.prepareStatement("DELETE FROM AsientosContables WHERE descripcion LIKE ?")) {
            ps.setString(1, "%" + etiqueta + "%");
            ps.executeUpdate();
        }
    }

    private int contarCopias() throws SQLException {
        try (Connection c = conexion();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM AsientosContables WHERE descripcion = ?")) {
            ps.setString(1, "COPIA - " + etiqueta + " prueba");
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    @Test
    void S01_duplicar_desde_la_pantalla_crea_una_copia() throws SQLException {
        page.navigate(BASE + "/login");
        page.fill("input[name='username']", EMAIL);
        page.fill("input[name='password']", CLAVE);
        page.click("button[type='submit']");
        page.waitForURL(url -> !url.contains("/login"));

        page.navigate(BASE + "/dashboard");
        page.evaluate("showPanel('asientos')");
        page.waitForTimeout(1000);
        int antes = contarCopias();

        page.locator("tr", new Page.LocatorOptions().setHasText(etiqueta))
            .locator("button[title='Duplicar']").click();
        page.waitForTimeout(2500);

        assertEquals(antes + 1, contarCopias());
    }
}
