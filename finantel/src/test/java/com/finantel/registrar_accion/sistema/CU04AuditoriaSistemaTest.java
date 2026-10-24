package com.finantel.registrar_accion.sistema;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class CU04AuditoriaSistemaTest {

    private static final String BASE = "http://localhost:8080";
    private static final String URL_BD = "jdbc:sqlserver://localhost:1433;databaseName=FINANTEL_TEST;encrypt=false;trustServerCertificate=true";
    private static final String EMAIL = "prueba@finantel.pe";
    private static final String CLAVE = "clave123";

    private static Playwright playwright;
    private static Browser browser;
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

    @BeforeEach
    void nuevaPagina() {
        page = browser.newPage();
    }

    @AfterEach
    void cerrarPagina() {
        page.close();
    }

    private int contarLoginsExitosos() throws SQLException {
        try (Connection c = DriverManager.getConnection(URL_BD, "sa", "admin098");
             PreparedStatement ps = c.prepareStatement(
                 "SELECT COUNT(*) FROM AuditoriaAcciones WHERE usuario = ? AND accion = 'Login exitoso'")) {
            ps.setString(1, EMAIL);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    @Test
    void S01_login_exitoso_queda_registrado_en_auditoria() throws SQLException {
        int antes = contarLoginsExitosos();

        page.navigate(BASE + "/login");
        page.fill("input[name='username']", EMAIL);
        page.fill("input[name='password']", CLAVE);
        page.click("button[type='submit']");
        page.waitForURL(url -> !url.contains("/login"));

        assertEquals(antes + 1, contarLoginsExitosos());
    }
}