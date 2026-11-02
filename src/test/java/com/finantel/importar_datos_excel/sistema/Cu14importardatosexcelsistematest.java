package com.finantel.importar_datos_excel.sistema;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class CU14ImportarDatosExcelSistemaTest {

    private static final String BASE = "http://localhost:8080";
    private static final String EMAIL = "prueba@finantel.pe";
    private static final String CLAVE = "clave123";
    private static final String URL_BD =
        "jdbc:sqlserver://localhost:1433;databaseName=FINANTEL_TEST;encrypt=false;trustServerCertificate=true";
    private static final String MARCA = "CU14 sistema";

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
    void limpiar() throws SQLException {
        if (page != null) page.close();
        try (Connection c = DriverManager.getConnection(URL_BD, "sa", "admin098");
             PreparedStatement ps = c.prepareStatement(
                 "DELETE FROM AsientosContables WHERE descripcion = ? AND usuario = ?")) {
            ps.setString(1, MARCA);
            ps.setString(2, EMAIL);
            ps.executeUpdate();
        }
    }

    @Test
    void S01_subir_un_csv_muestra_el_resultado_de_la_importacion() throws Exception {
        page.navigate(BASE + "/login");
        page.fill("input[name='username']", EMAIL);
        page.fill("input[name='password']", CLAVE);
        page.click("button[type='submit']");
        page.waitForURL(url -> !url.contains("/login"));

        page.navigate(BASE + "/dashboard");

        Path csv = Files.createTempFile("cu14-", ".csv");
        Files.writeString(csv,
            "tipo,descripcion,debe,haber\nCierre," + MARCA + ",100,100\n",
            StandardCharsets.UTF_8);

        Response respuesta = page.waitForResponse(
            r -> r.url().contains("/web/importar-excel"),
            () -> page.setInputFiles("#file-excel-input", csv));

        String cuerpo = respuesta.text();
        assertTrue(cuerpo.contains("importados"),
            "La importación no respondió con éxito: " + cuerpo);
    }
}