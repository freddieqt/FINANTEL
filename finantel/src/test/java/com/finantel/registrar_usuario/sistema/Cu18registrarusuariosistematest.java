package com.finantel.registrar_usuario.sistema;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class CU18RegistrarUsuarioSistemaTest {

    private static final String BASE = "http://localhost:8080";
    private static final String ADMIN_EMAIL = "admin.prueba@finantel.pe";
    private static final String ADMIN_CLAVE = "clave123";
    private static final String NUEVO_EMAIL = "cu18.s01@finantel.pe";
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
             PreparedStatement ps = c.prepareStatement("DELETE FROM Usuarios WHERE email = ?")) {
            ps.setString(1, NUEVO_EMAIL);
            ps.executeUpdate();
        }
    }

    @Test
    void S01_admin_registra_un_usuario_nuevo_desde_el_modal() {
        page.navigate(BASE + "/login");
        page.fill("input[name='username']", ADMIN_EMAIL);
        page.fill("input[name='password']", ADMIN_CLAVE);
        page.click("button[type='submit']");
        page.waitForURL(url -> !url.contains("/login"));

        page.navigate(BASE + "/dashboard");
        page.evaluate("openModalUsuario()");

        page.fill("#modal-usuario input[type=text]", "CU18 Sistema");
        page.fill("#modal-usuario input[type=email]", NUEVO_EMAIL);
        page.fill("#modal-usuario input[type=password]", "clave123");
        page.selectOption("#modal-usuario select", "Auditor");
        page.fill("#modal-dni", "12345678");
        page.fill("#modal-tel", "987654321");

        Response respuesta = page.waitForResponse(
            r -> r.url().contains("/web/usuarios") && "POST".equals(r.request().method()),
            () -> page.click("#modal-usuario button:has-text('Crear Usuario')")
        );

        assertEquals(200, respuesta.status(), "El usuario no se registró desde la pantalla");
    }
}