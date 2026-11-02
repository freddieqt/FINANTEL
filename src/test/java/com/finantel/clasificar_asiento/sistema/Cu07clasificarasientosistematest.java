package com.finantel.clasificar_asiento.sistema;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CU07ClasificarAsientoSistemaTest {

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
    void S01_la_sesion_iniciada_obtiene_los_tipos_de_clasificacion() {
        page.navigate(BASE + "/login");
        page.fill("input[name='username']", EMAIL);
        page.fill("input[name='password']", CLAVE);
        page.click("button[type='submit']");
        page.waitForURL(url -> !url.contains("/login"));

        page.navigate(BASE + "/dashboard");

        // Diagnóstico temporal para verificar el estatus y cuerpo de la respuesta
        String respuesta = (String) page.evaluate(
            "async () => { const r = await fetch('/clasificar-asiento/tipos'); return r.status + ' ' + await r.text(); }"
        );
        System.out.println("RESPUESTA: " + respuesta);
    }
}