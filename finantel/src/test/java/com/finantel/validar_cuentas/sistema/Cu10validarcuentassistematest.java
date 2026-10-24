package com.finantel.validar_cuentas.sistema;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.RequestOptions;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CU10ValidarCuentasSistemaTest {

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
    void S01_la_validacion_de_cuentas_responde_con_jwt() {
        APIRequestContext api = playwright.request().newContext(
            new APIRequest.NewContextOptions().setBaseURL(BASE));

        APIResponse login = api.post("/auth/login",
            RequestOptions.create().setData(Map.of("email", EMAIL, "password", CLAVE)));

        assertEquals(200, login.status());

        String cuerpo = new String(login.body(), java.nio.charset.StandardCharsets.UTF_8);
        java.util.regex.Matcher m = java.util.regex.Pattern
            .compile("\"token\"\\s*:\\s*\"([^\"]+)\"")
            .matcher(cuerpo);
        assertTrue(m.find(), "La respuesta de login no contiene token");
        String token = m.group(1);

        APIResponse respuesta = api.get("/validar-cuentas",
            RequestOptions.create().setHeader("Authorization", "Bearer " + token));

        assertEquals(200, respuesta.status());

        api.dispose();
    }
}