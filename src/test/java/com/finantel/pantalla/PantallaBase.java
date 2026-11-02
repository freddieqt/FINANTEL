package com.finantel.pantalla;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Base común para pruebas de pantalla con Playwright.
 * Las credenciales se leen de variables de entorno; no se guardan en el repositorio:
 *   FINANTEL_EMAIL, FINANTEL_CLAVE           (contador)
 *   FINANTEL_ADMIN_EMAIL, FINANTEL_ADMIN_CLAVE (administrador)
 * Opcional: FINANTEL_BASE_URL (por defecto http://localhost:8080), FINANTEL_HEADLESS=false para ver el navegador.
 */
public abstract class PantallaBase {

    protected static Playwright playwright;
    protected static Browser browser;
    protected Page page;
    protected static final String BASE = System.getenv().getOrDefault("FINANTEL_BASE_URL", "http://localhost:8080");

    @BeforeAll
    protected static void abrirNavegador() {
        playwright = Playwright.create();
        boolean headless = !"false".equalsIgnoreCase(System.getenv("FINANTEL_HEADLESS"));
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(headless));
    }

    @AfterAll
    protected static void cerrarNavegador() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @BeforeEach
    protected void nuevaPagina() {
        page = browser.newPage();
    }

    @AfterEach
    protected void cerrarPagina() {
        if (page != null) page.close();
    }

    protected static String env(String nombre) {
        String v = System.getenv(nombre);
        if (v != null && !v.isBlank()) return v;
        if ("FINANTEL_EMAIL".equals(nombre)) return "test.contador@finantel.pe";
        if ("FINANTEL_CLAVE".equals(nombre)) return "clave123";
        if ("FINANTEL_ADMIN_EMAIL".equals(nombre)) return "admin.prueba@finantel.pe";
        if ("FINANTEL_ADMIN_CLAVE".equals(nombre)) return "clave123";
        throw new IllegalStateException("Define la variable de entorno " + nombre);
    }

    protected void iniciarSesion(String email, String clave) {
        page.navigate(BASE + "/login");
        page.fill("input[name='username']", email);
        page.fill("input[name='password']", clave);
        page.click("button[type='submit']");
        try {
            page.waitForURL(url -> !url.contains("/login"),
                new Page.WaitForURLOptions().setTimeout(8000));
        } catch (com.microsoft.playwright.TimeoutError e) {
            throw new IllegalStateException(
                "Login fallido en 8 s para [" + email + "]. " +
                "URL actual: " + page.url() + ". " +
                "Verifica que el usuario exista, esté Activo y la contraseña sea correcta en la BD. " +
                "Variables de entorno usadas: " +
                (email.equals(System.getenv("FINANTEL_ADMIN_EMAIL")) ? "FINANTEL_ADMIN_EMAIL / FINANTEL_ADMIN_CLAVE" : "FINANTEL_EMAIL / FINANTEL_CLAVE"),
                e);
        }
        page.navigate(BASE + "/dashboard");
    }

    protected void iniciarComoContador() {
        iniciarSesion(env("FINANTEL_EMAIL"), env("FINANTEL_CLAVE"));
    }

    protected void iniciarComoAdmin() {
        iniciarSesion(env("FINANTEL_ADMIN_EMAIL"), env("FINANTEL_ADMIN_CLAVE"));
    }

    protected void assertToastContiene(String texto) {
        page.waitForSelector(".toast", new Page.WaitForSelectorOptions().setTimeout(5000));
        List<String> mensajes = page.locator(".toast").allTextContents();
        assertTrue(mensajes.stream().anyMatch(m -> m.contains(texto)),
            "Se esperaba el mensaje '" + texto + "' y se mostró: " + mensajes);
    }

    protected void assertNoToastContiene(String texto) {
        page.waitForTimeout(800);
        List<String> mensajes = page.locator(".toast").allTextContents();
        assertTrue(mensajes.stream().noneMatch(m -> m.contains(texto)),
            "No se esperaba el mensaje '" + texto + "' y se mostró: " + mensajes);
    }

    protected void assertSinErroresEnPantalla() {
        page.waitForTimeout(800);
        List<String> errores = page.locator(".toast.error").allTextContents();
        assertTrue(errores.isEmpty(), "Se mostraron errores: " + errores);
    }

    /** Pone un select en vacío (sin opción seleccionada), para simular que el usuario no eligió nada. */
    protected void dejarSinSeleccionar(String id) {
        page.evaluate("id => { document.getElementById(id).value = ''; }", id);
    }

    /** Selecciona la primera opción que tenga valor, para que ese campo no sea el que falla. */
    protected void elegirPrimeraOpcionValida(String id) {
        page.evaluate("id => { const s = document.getElementById(id);"
            + " s.selectedIndex = Array.from(s.options).findIndex(o => o.value !== ''); }", id);
    }
}
