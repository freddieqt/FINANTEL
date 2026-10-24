package com.finantel.emitir_comprobantes.sistema;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class EmitirComprobantesSistemaTest {

    private static final String BASE = "http://localhost:8080";
    private static final String EMAIL = "admin.prueba@finantel.pe";
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
    void S01_el_usuario_emite_un_comprobante_desde_la_pantalla() {
        page.navigate(BASE + "/login");
        page.fill("input[name='username']", EMAIL);
        page.fill("input[name='password']", CLAVE);
        page.click("button[type='submit']");
        page.waitForURL(url -> !url.contains("/login"));

        page.navigate(BASE + "/dashboard");
        page.evaluate("emitirComprobante()");

        page.fill("#comp-ndoc", "20123456789");
        page.fill("#comp-cliente", "Empresa Sistema SAC");
        page.fill("#comp-concepto", "Servicio de prueba");
        page.fill("#comp-cantidad", "1");
        page.fill("#comp-precio", "100");
        page.evaluate("document.getElementById('comp-correlativo').value = '" + (System.currentTimeMillis() % 100000) + "'");

        Response respuesta = page.waitForResponse(
            r -> r.url().contains("/web/comprobantes") && "POST".equals(r.request().method()),
            () -> page.click("button[onclick='confirmarComprobante()']")
        );

        assertEquals(200, respuesta.status(), "El comprobante no se emitió desde la pantalla");
    }
}