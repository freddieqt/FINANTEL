package com.finantel.generar_estados_financieros.sistema;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class CU11GenerarEstadosSistemaTest {

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
    void S01_el_balance_general_se_muestra_en_pantalla_con_su_total() {
        page.navigate(BASE + "/login");
        page.fill("input[name='username']", EMAIL);
        page.fill("input[name='password']", CLAVE);
        page.click("button[type='submit']");
        page.waitForURL(url -> !url.contains("/login"));

        page.navigate(BASE + "/dashboard");
        page.evaluate("renderBalance()");

        page.waitForSelector("#tbl-activos td:has-text('TOTAL')",
        	    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED));

        String totalActivos = page.locator("#tbl-activos tr:last-child").innerText();
        assertTrue(totalActivos.contains("TOTAL"), "La tabla de activos no muestra la fila TOTAL");

        String alerta = page.locator("#balance-alert").innerText();
        assertFalse(alerta.isBlank(), "El balance no muestra el resultado de cuadre");
    }
}