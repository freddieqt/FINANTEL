package com.finantel.exportar_reportes.sistema;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;
import com.microsoft.playwright.options.WaitForSelectorState;
import static org.junit.jupiter.api.Assertions.*;

class CU12ExportarReportesSistemaTest {

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
    void S01_exportar_auditoria_descarga_un_csv() {
        page.navigate(BASE + "/login");
        page.fill("input[name='username']", EMAIL);
        page.fill("input[name='password']", CLAVE);
        page.click("button[type='submit']");
        page.waitForURL(url -> !url.contains("/login"));

        page.navigate(BASE + "/dashboard");
        page.evaluate("showPanel('auditoria')");
        page.waitForSelector("#sub-auditoria table tbody tr td",
        	    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED));

        Download descarga = page.waitForDownload(() -> page.evaluate("exportAuditCSV()"));

        String nombre = descarga.suggestedFilename();
        assertTrue(nombre.startsWith("FINANTEL_Auditoria_"), "Nombre inesperado: " + nombre);
        assertTrue(nombre.endsWith(".csv"), "La descarga no es CSV: " + nombre);
    }
}