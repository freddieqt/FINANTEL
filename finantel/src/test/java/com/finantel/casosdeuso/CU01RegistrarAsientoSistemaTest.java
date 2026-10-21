package com.finantel.casosdeuso;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.SelectOption;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CU01RegistrarAsientoSistemaTest {

    @LocalServerPort
    private int port;

    @Autowired
    private com.finantel.registrar_usuario.dominio.repository.UsuarioRepository usuarioRepo;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder encoder;

    private static Playwright playwright;
    private static Browser browser;
    private Page page;

    @BeforeAll
    static void abrirNavegador() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
            new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(300)
        );
    }

    @AfterAll
    static void cerrarNavegador() {
        browser.close();
        playwright.close();
    }

    @BeforeEach
    void nuevaPagina() {
        page = browser.newPage();
        page.setDefaultTimeout(10000);
    }

    @AfterEach
    void cerrarPagina() {
        page.close();
    }

    @Test
    @DisplayName("CU01 · Contador registra un asiento válido desde la pantalla")
    void registrarAsientoValido() {
        String base = "http://localhost:" + port;
        
        // Login
        page.navigate(base + "/login");
        page.fill("input[name=username]", "test.contador@finantel.pe");
        page.fill("input[name=password]", "goku123");
        page.click("button[type=submit]");
        page.waitForLoadState();

        if (page.url().contains("/login")) {
            fail("Login fallido. URL: " + page.url());
        }

        // Ir a asientos desde el dashboard
        page.navigate(base + "/dashboard");
        page.click("text=Asientos Contables");

        // Cabecera (apertura del modal, carga de periodos y selección por índice)
        page.click("text=+ Nuevo Asiento");
        page.waitForSelector("#modal-asiento.open");
        page.evaluate("cargarPeriodos()");

        page.waitForFunction("document.querySelectorAll('#periodoId option').length > 1");
        page.selectOption("#periodoId", new SelectOption().setIndex(1));
        page.fill("#asiento-fecha-contable", "2026-10-15");
        
        page.selectOption("#asiento-tipo", "Operación ordinaria");
        page.selectOption("#asiento-clasificacion", "Activo");
        page.selectOption("#asiento-desc", "Cobro cuota crédito");
        page.selectOption("#asiento-tipo-comprobante", "F");
        page.fill("#asiento-ref-comprobante", "001-00123");

        // Partidas (dos líneas, mismo monto)
        var debe = page.locator("#asiento-lineas input[type=number]");
        debe.nth(0).fill("100");
        debe.nth(1).fill("100");
        debe.nth(2).fill("100");
        debe.nth(3).fill("100");

        // Medición PRE y POST de guardado
        System.out.println("PRE=" + page.innerText("body").contains("no cuadra"));
        page.click("text=💾 Guardar Asiento");

        page.waitForTimeout(1000);
        System.out.println("POST=" + page.innerText("body").contains("no cuadra"));
        
        assertFalse(page.innerText("body").contains("no cuadra"), "El asiento no debería descuadrar");
    }
}