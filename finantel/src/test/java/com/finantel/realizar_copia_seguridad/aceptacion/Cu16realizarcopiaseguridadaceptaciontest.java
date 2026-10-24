package com.finantel.realizar_copia_seguridad.aceptacion;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CU16RealizarCopiaSeguridadAceptacionTest {

    private static final String URL_BD =
        "jdbc:sqlserver://localhost:1433;databaseName=FINANTEL_TEST;encrypt=false;trustServerCertificate=true";
    private static final String USUARIO = "prueba@finantel.pe";

    @Autowired
    private MockMvc mvc;

    private static Connection conexion() throws SQLException {
        return DriverManager.getConnection(URL_BD, "sa", "admin098");
    }

    @AfterEach
    void limpiar() throws SQLException {
        try (Connection c = conexion();
             PreparedStatement ps = c.prepareStatement(
                 "DELETE FROM RespaldosBD WHERE usuario = ? AND tipo = 'Manual'")) {
            ps.setString(1, USUARIO);
            ps.executeUpdate();
        }
    }

    @Test
    void HA01_realizar_copia_queda_registrada_en_la_base() throws Exception {
        mvc.perform(post("/copia-seguridad")
                .with(user(USUARIO))
                .with(csrf()))
            .andExpect(status().isOk());

        try (Connection c = conexion();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT COUNT(*) FROM RespaldosBD WHERE usuario = ? AND tipo = 'Manual' AND estado = 'Completado'")) {
            ps.setString(1, USUARIO);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                assertEquals(1, rs.getInt(1));
            }
        }
    }

    @Test
    void HA02_la_lista_de_copias_muestra_la_copia_completada() throws Exception {
        mvc.perform(post("/copia-seguridad")
                .with(user(USUARIO))
                .with(csrf()))
            .andExpect(status().isOk());

        mvc.perform(get("/copia-seguridad")
                .with(user(USUARIO)))
            .andExpect(status().isOk())
            .andExpect(content().string(Matchers.containsString("Completado")));
    }
}