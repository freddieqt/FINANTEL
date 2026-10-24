package com.finantel.importar_datos_excel.aceptacion;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CU14ImportarDatosExcelAceptacionTest {

    private static final String URL_BD =
        "jdbc:sqlserver://localhost:1433;databaseName=FINANTEL_TEST;encrypt=false;trustServerCertificate=true";

    private static final String MARCA = "CU14 aceptacion";

    @Autowired
    private MockMvc mvc;

    private static Connection conexion() throws SQLException {
        return DriverManager.getConnection(URL_BD, "sa", "admin098");
    }

    @AfterEach
    void limpiar() throws SQLException {
        try (Connection c = conexion();
             PreparedStatement ps = c.prepareStatement(
                 "DELETE FROM AsientosContables WHERE descripcion = ? AND usuario = ?")) {
            ps.setString(1, MARCA);
            ps.setString(2, "prueba@finantel.pe");
            ps.executeUpdate();
        }
    }

    @Test
    void HA01_importar_un_csv_valido_reporta_los_asientos_importados() throws Exception {
        String csv = "tipo,descripcion,debe,haber\nCierre," + MARCA + ",100,100\n";
        MockMultipartFile archivo = new MockMultipartFile("archivo", "datos.csv", "text/csv",
            csv.getBytes(StandardCharsets.UTF_8));

        mvc.perform(multipart("/web/importar-excel")
                .file(archivo)
                .with(user("prueba@finantel.pe"))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(content().string(Matchers.containsString("1 asientos importados correctamente")));
    }

    @Test
    void HA02_importar_un_archivo_vacio_no_registra_nada() throws Exception {
        MockMultipartFile vacio = new MockMultipartFile("archivo", "datos.csv", "text/csv", new byte[0]);

        mvc.perform(multipart("/web/importar-excel")
                .file(vacio)
                .with(user("prueba@finantel.pe"))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(content().string(Matchers.containsString("Error: archivo vacío")));
    }
}