package com.finantel.gestionar_roles.sistema;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class CU03GestionarRolesSistemaTest {

    private static final String BASE = "http://localhost:8080";
    private static Playwright playwright;
    private static Browser browser;
    private Page page;

    @BeforeAll
    static void iniciar() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
    }

    @AfterAll
    static void cerrar() {
        browser.close();
        playwright.close();
    }

    @BeforeEach
    void nuevaPagina() {
        page = browser.newPage();
    }

    @AfterEach
    void cerrarPagina() {
        page.close();
    }

    @Test
    void S01_sin_sesion_admin_redirige_al_login() {
        page.navigate(BASE + "/admin");
        page.waitForURL("**/login");
        assertTrue(page.url().endsWith("/login"));
    }
}