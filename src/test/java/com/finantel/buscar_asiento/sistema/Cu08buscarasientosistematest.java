package com.finantel.buscar_asiento.sistema;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CU08BuscarAsientoSistemaTest {

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
    void S01_la_busqueda_por_tipo_responde_con_token_de_la_api() {
        page.navigate(BASE + "/login");
        page.fill("input[name='username']", EMAIL);
        page.fill("input[name='password']", CLAVE);
        page.click("button[type='submit']");
        page.waitForURL(url -> !url.contains("/login"));

        Object cantidad = page.evaluate(
            "async () => {"
            + " const login = await fetch('/auth/login', {method: 'POST',"
            + "   headers: {'Content-Type': 'application/json'},"
            + "   body: JSON.stringify({email: '" + EMAIL + "', password: '" + CLAVE + "'})});"
            + " const token = (await login.json()).token;"
            + " const r = await fetch('/buscar-asiento/tipo/Cierre',"
            + "   {headers: {'Authorization': 'Bearer ' + token}});"
            + " const j = await r.json();"
            + " return Array.isArray(j) ? j.length : -1; }"
        );

        assertTrue(((Number) cantidad).intValue() >= 0,
            "La búsqueda no devolvió un arreglo");
    }
}