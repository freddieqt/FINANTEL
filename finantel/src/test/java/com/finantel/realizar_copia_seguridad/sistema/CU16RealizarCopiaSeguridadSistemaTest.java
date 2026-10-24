package com.finantel.realizar_copia_seguridad.sistema;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class CU16RealizarCopiaSeguridadSistemaTest {

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
    void S01_admin_crea_respaldo_manual_desde_la_pantalla() {
        page.navigate(BASE + "/login");
        page.fill("input[name='username']", EMAIL);
        page.fill("input[name='password']", CLAVE);
        page.click("button[type='submit']");
        page.waitForURL(url -> !url.contains("/login"));

        page.navigate(BASE + "/dashboard");
        page.evaluate("showPanel('admin'); document.querySelectorAll('.admin-sub').forEach(s => s.style.display = 'none'); document.getElementById('sub-respaldos').style.display = 'block';");

        Response respuesta = page.waitForResponse(
        	    r -> r.url().contains("/web/respaldos") && "POST".equals(r.request().method()),
        	    () -> page.locator("#sub-respaldos button:has-text('Respaldo')").dispatchEvent("click")
        	);
        assertEquals(200, respuesta.status(), "El respaldo manual no respondió 200");

        page.waitForSelector("#sub-respaldos td:has-text('OK')",
            new Page.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED));
    }
}