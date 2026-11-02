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

    private void iniciarComoAdmin() {
        page.navigate(BASE + "/login");
        page.fill("input[name='username']", ADMIN_EMAIL);
        page.fill("input[name='password']", ADMIN_CLAVE);
        page.click("button[type='submit']");
        page.waitForURL(url -> !url.contains("/login"));
        page.navigate(BASE + "/dashboard");
    }

    private void abrirNuevoUsuario() {
        iniciarComoAdmin();
        page.evaluate("openModalUsuario()");
        page.waitForSelector("#modal-usuario.open");
    }

    private void crear() {
        page.click("#modal-usuario button:has-text('Crear Usuario')");
    }

    private void assertToastContiene(String mensaje) {
        Locator toast = page.locator(".toast, .alert, .notification, .swal2-popup, .invalid-feedback, .error-message");
        assertTrue(toast.filter(new Locator.FilterOptions().setHasText(mensaje)).isVisible(),
            "Se esperaba encontrar el mensaje de error: " + mensaje);
    }

    private void assertNoToastContiene(String mensaje) {
        Locator toast = page.locator(".toast, .alert, .notification, .swal2-popup, .invalid-feedback, .error-message");
        assertFalse(toast.filter(new Locator.FilterOptions().setHasText(mensaje)).isVisible(),
            "No se esperaba encontrar el mensaje de error: " + mensaje);
    }

    @Test
    void S01_admin_registra_un_usuario_nuevo_desde_el_modal() {
        iniciarComoAdmin();
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

    // ENTRADA: [CU18 pantalla] nombre = "" (vacío); email = nuevo@finantel.pe; contraseña = 123456; rol = válido
    // RESULTADO: Se muestra el mensaje "Nombre es obligatorio"
    @Test
    void nombre_vacio_muestra_error() {
        abrirNuevoUsuario();
        page.locator("#modal-usuario input[type=text]").first().fill("");
        page.fill("#modal-usuario input[type=email]", "nuevo@finantel.pe");
        page.fill("#modal-usuario input[type=password]", "123456");
        page.evaluate("() => { const s = document.querySelector('#modal-usuario select'); s.selectedIndex = Array.from(s.options).findIndex(o => o.value !== ''); }");
        crear();
        assertToastContiene("Nombre es obligatorio");
    }

    // ENTRADA: [CU18 pantalla] nombre = "Prueba"; email = prueba.finantel.pe (sin @); contraseña = 123456; rol = válido
    // RESULTADO: Se muestra el mensaje "Email inválido"
    @Test
    void email_sin_arroba_muestra_error() {
        abrirNuevoUsuario();
        page.locator("#modal-usuario input[type=text]").first().fill("Prueba");
        page.fill("#modal-usuario input[type=email]", "prueba.finantel.pe");
        page.fill("#modal-usuario input[type=password]", "123456");
        page.evaluate("() => { const s = document.querySelector('#modal-usuario select'); s.selectedIndex = Array.from(s.options).findIndex(o => o.value !== ''); }");
        crear();
        assertToastContiene("Email inválido");
    }

    // ENTRADA: [CU18 pantalla] nombre = "Prueba"; email = nuevo@finantel.pe; contraseña = 123456; rol = opción vacía
    // RESULTADO: Se muestra el mensaje "Selecciona un rol"
    @Test
    void rol_sin_seleccionar_muestra_error() {
        abrirNuevoUsuario();
        page.locator("#modal-usuario input[type=text]").first().fill("Prueba");
        page.fill("#modal-usuario input[type=email]", "nuevo@finantel.pe");
        page.fill("#modal-usuario input[type=password]", "123456");
        page.evaluate("() => { document.querySelector('#modal-usuario select').selectedIndex = 0; }");
        crear();
        assertToastContiene("Selecciona un rol");
    }

    // ENTRADA: [CU18 pantalla] nombre = "Prueba"; email = nuevo@finantel.pe; contraseña = 12345 (5 caracteres); rol = válido
    // RESULTADO: Se muestra el mensaje "Contraseña mín. 6 caracteres"
    @Test
    void contrasena_de_5_caracteres_muestra_error() {
        abrirNuevoUsuario();
        page.locator("#modal-usuario input[type=text]").first().fill("Prueba");
        page.fill("#modal-usuario input[type=email]", "nuevo@finantel.pe");
        page.fill("#modal-usuario input[type=password]", "12345");
        page.evaluate("() => { const s = document.querySelector('#modal-usuario select'); s.selectedIndex = Array.from(s.options).findIndex(o => o.value !== ''); }");
        crear();
        assertToastContiene("Contraseña mín. 6 caracteres");
    }

    // ENTRADA: [Cambiar contraseña pantalla] contraseña actual = "" (vacía); nueva = 123456; confirmar = 123456
    // RESULTADO: Se muestra el mensaje "Ingresa tu contraseña actual"
    @Test
    void contrasena_actual_vacia_muestra_error() {
        iniciarComoAdmin();
        page.evaluate("cambiarPassword(1)");
        page.waitForSelector("#cp-actual");
        page.fill("#cp-actual", "");
        page.fill("#cp-nueva", "123456");
        page.fill("#cp-confirmar", "123456");
        page.click("button[onclick^='confirmarCambioPassword']");
        assertToastContiene("Ingresa tu contraseña actual");
    }

    // ENTRADA: [Cambiar contraseña pantalla] contraseña actual = "clave-actual"; nueva = 12345 (5 caracteres); confirmar = 12345
    // RESULTADO: Se muestra el mensaje "La nueva contraseña debe tener mínimo 6 caracteres"
    @Test
    void contrasena_nueva_de_5_caracteres_muestra_error() {
        iniciarComoAdmin();
        page.evaluate("cambiarPassword(1)");
        page.waitForSelector("#cp-actual");
        page.fill("#cp-actual", "clave-actual");
        page.fill("#cp-nueva", "12345");
        page.fill("#cp-confirmar", "12345");
        page.click("button[onclick^='confirmarCambioPassword']");
        assertToastContiene("La nueva contraseña debe tener mínimo 6 caracteres");
    }

    // ENTRADA: [Cambiar contraseña pantalla] contraseña actual = "clave-actual"; nueva = 123456; confirmar = 123457
    // RESULTADO: Se muestra el mensaje "Las contraseñas no coinciden"
    @Test
    void confirmacion_distinta_muestra_error() {
        iniciarComoAdmin();
        page.evaluate("cambiarPassword(1)");
        page.waitForSelector("#cp-actual");
        page.fill("#cp-actual", "clave-actual");
        page.fill("#cp-nueva", "123456");
        page.fill("#cp-confirmar", "123457");
        page.click("button[onclick^='confirmarCambioPassword']");
        assertToastContiene("Las contraseñas no coinciden");
    }

    // ENTRADA: [Cambiar contraseña pantalla] contraseña actual = "clave-actual"; nueva = 123456; confirmar = 123456
    // RESULTADO: No se muestra ningún error de validación de la pantalla
    @Test
    void contrasena_nueva_de_6_caracteres_pasa_la_validacion() {
        iniciarComoAdmin();
        page.evaluate("cambiarPassword(1)");
        page.waitForSelector("#cp-actual");
        page.fill("#cp-actual", "clave-actual");
        page.fill("#cp-nueva", "123456");
        page.fill("#cp-confirmar", "123456");
        page.click("button[onclick^='confirmarCambioPassword']");
        assertNoToastContiene("mínimo 6 caracteres");
        assertNoToastContiene("no coinciden");
    }
}