package com.finantel.buscar_asiento.aceptacion;

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
import java.sql.SQLException;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CU08BuscarAsientoAceptacionTest {

    private static final String URL_BD =
        "jdbc:sqlserver://localhost:1433;databaseName=FINANTEL_TEST;encrypt=false;trustServerCertificate=true";

    @Autowired
    private MockMvc mvc;

    private String correlativo;

    private static Connection conexion() throws SQLException {
        return DriverManager.getConnection(URL_BD, "sa", "admin098");
    }

    @BeforeEach
    void preparar() throws SQLException {
        correlativo = "CU08-" + (System.nanoTime() % 100000);

        String sql = """
            INSERT INTO AsientosContables
                (descripcion, debe, haber, tipo, estado, usuario,
                 cuenta_contable, centro_costo, moneda, tipo_cambio,
                 periodo_id, fecha_registro, numero_correlativo)
            VALUES
                ('CU08 aceptacion', 100, 100, 'Cierre',
                 'Activo', 'prueba@finantel.pe', '1011', 'General', 'PEN', 1.0,
                 1, GETDATE(), ?)
            """;

        try (Connection c = conexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, correlativo);
            ps.executeUpdate();
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
    void HA01_al_buscar_por_tipo_aparece_el_asiento_registrado() throws Exception {
        mvc.perform(get("/buscar-asiento/tipo/Cierre")
                .with(user("prueba@finantel.pe")))
            .andExpect(status().isOk())
            .andExpect(content().string(Matchers.containsString(correlativo)));
    }

    @Test
    void HA02_al_buscar_por_estado_anulado_no_aparece_el_asiento_activo() throws Exception {
        mvc.perform(get("/buscar-asiento/estado/Anulado")
                .with(user("prueba@finantel.pe")))
            .andExpect(status().isOk())
            .andExpect(content().string(Matchers.not(Matchers.containsString(correlativo))));
    }
}