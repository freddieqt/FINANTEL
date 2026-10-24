package com.finantel.generar_libro_contable.sistema;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class CU21GenerarLibroContableSistemaTest {

    private static final String BASE = "http://localhost:8080";
    private static final String EMAIL = "prueba@finantel.pe";
    private static final String CLAVE = "clave123";

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
    void limpiar() {
        if (page != null) page.close();
    }

    @Test
    void S01_el_libro_contable_muestra_los_asientos_activos() {
        page.navigate(BASE + "/login");
        page.fill("input[name='username']", EMAIL);
        page.fill("input[name='password']", CLAVE);
        page.click("button[type='submit']");
        page.waitForURL(url -> !url.contains("/login"));

        page.navigate(BASE + "/dashboard");

        Response respuesta = page.waitForResponse(
            r -> r.url().contains("/web/asientos"),
            () -> page.evaluate("renderLibro()")
        );
        assertEquals(200, respuesta.status(), "El libro no pudo cargar los asientos");

        page.waitForFunction(
            "document.getElementById('libro-entries') && document.getElementById('libro-entries').textContent.trim().length > 0"
        );

        String contenido = page.locator("#libro-entries").textContent();
        assertFalse(contenido.isBlank(), "El libro contable aparece vacío");
    }
}