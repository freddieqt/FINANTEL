package com.finantel.autenticar_usuario.sistema;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CU02AutenticarUsuarioSistemaTest {

    private static final String BASE = "http://localhost:8080";
    private static final String EMAIL_ACTIVO = "prueba@finantel.pe";
    private static final String EMAIL_INACTIVO = "inactivo@finantel.pe";
    private static final String PASSWORD = "clave123";

    private static Playwright playwright;
    private static Browser browser;
    private Page page;

    @BeforeAll
    static void abrirNavegador() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
    }

    @AfterAll
    static void cerrarNavegador() {
        browser.close();
        playwright.close();
    }

    @BeforeEach
    void abrirPagina() {
        page = browser.newPage();
        page.navigate(BASE + "/login");
    }

    @AfterEach
    void cerrarPagina() {
        page.close();
    }

    private void iniciarSesion(String email, String password) {
        page.fill("input[name='username']", email);
        page.fill("input[name='password']", password);
        page.click("button[type='submit']");
    }

    @Test
    @DisplayName("S-01 · Login correcto entra al sistema")
    void s01_loginCorrecto_entraAlSistema() {
        iniciarSesion(EMAIL_ACTIVO, PASSWORD);
        page.waitForURL(url -> !url.contains("/login"));
        assertFalse(page.url().contains("/login"));
    }

    @Test
    @DisplayName("S-02 · Contraseña incorrecta muestra error y no entra")
    void s02_contrasenaIncorrecta_muestraError() {
        iniciarSesion(EMAIL_ACTIVO, "otraClave");
        assertThat(page.locator(".alert-danger")).containsText("Correo o contraseña incorrectos");
    }

    @Test
    @DisplayName("S-03 · Usuario inactivo con contraseña correcta muestra error y no entra")
    void s03_usuarioInactivo_muestraError() {
        iniciarSesion(EMAIL_INACTIVO, PASSWORD);
        assertThat(page.locator(".alert-danger")).containsText("Correo o contraseña incorrectos");
    }
}