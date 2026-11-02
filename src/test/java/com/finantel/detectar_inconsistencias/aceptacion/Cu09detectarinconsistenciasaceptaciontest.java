package com.finantel.detectar_inconsistencias.aceptacion;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CU09DetectarInconsistenciasAceptacionTest {

    private static final String URL_BD =
        "jdbc:sqlserver://localhost:1433;databaseName=FINANTEL_TEST;encrypt=false;trustServerCertificate=true";

    @Autowired
    private MockMvc mvc;

    private String correlativo;
    private int idAsiento;

    private static Connection conexion() throws SQLException {
        return DriverManager.getConnection(URL_BD, "sa", "admin098");
    }

    @BeforeEach
    void preparar() throws SQLException {
        correlativo = "CU09-" + (System.nanoTime() % 100000);

        // Asiento descuadrado insertado directamente en BD: la app no permite crearlo desde la pantalla.
        String insert = """
            INSERT INTO AsientosContables
                (descripcion, debe, haber, tipo, estado, usuario,
                 cuenta_contable, centro_costo, moneda, tipo_cambio,
                 periodo_id, fecha_registro, numero_correlativo)
            VALUES
                ('CU09 aceptacion', 100, 50, 'Operación ordinaria',
                 'Activo', 'prueba@finantel.pe', '1011', 'General', 'PEN', 1.0,
                 1, GETDATE(), ?)
            """;

        try (Connection c = conexion();
             PreparedStatement ps = c.prepareStatement(insert)) {
            ps.setString(1, correlativo);
            ps.executeUpdate();
        }

        try (Connection c = conexion();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT id FROM AsientosContables WHERE numero_correlativo = ?")) {
            ps.setString(1, correlativo);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                idAsiento = rs.getInt("id");
            }
        }
    }

    @AfterEach
    void limpiar() throws SQLException {
        try (Connection c = conexion();
             PreparedStatement ps = c.prepareStatement(
                 "DELETE FROM AsientosContables WHERE numero_correlativo = ?")) {
            ps.setString(1, correlativo);
            ps.executeUpdate();
        }
    }

    @Test
    void HA01_el_asiento_descuadrado_se_reporta_con_error() throws Exception {
        mvc.perform(get("/inconsistencias/" + idAsiento)
                .with(user("prueba@finantel.pe")))
            .andExpect(status().isOk())
            .andExpect(content().string(Matchers.containsString("Error:")));
    }

    @Test
    void HA02_la_lista_de_inconsistencias_incluye_el_desbalance() throws Exception {
        mvc.perform(get("/inconsistencias")
                .with(user("prueba@finantel.pe")))
            .andExpect(status().isOk())
            .andExpect(content().string(Matchers.containsString("Desbalance contable")));
    }
}