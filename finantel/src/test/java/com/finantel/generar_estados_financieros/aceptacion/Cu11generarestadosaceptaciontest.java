package com.finantel.generar_estados_financieros.aceptacion;

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
class CU11GenerarEstadosAceptacionTest {

    private static final String URL_BD =
        "jdbc:sqlserver://localhost:1433;databaseName=FINANTEL_TEST;encrypt=false;trustServerCertificate=true";

    @Autowired
    private MockMvc mvc;

    private String codigo;
    private String correlativo;

    private static Connection conexion() throws SQLException {
        return DriverManager.getConnection(URL_BD, "sa", "admin098");
    }

    @BeforeEach
    void preparar() throws SQLException {
        long n = System.nanoTime() % 100000;
        codigo = "1" + n;
        correlativo = "CU11-" + n;

        try (Connection c = conexion();
             PreparedStatement ps = c.prepareStatement("""
                 INSERT INTO CuentasContables
                     (codigo, descripcion, clasificacion, tipo, estado, ops_autorizadas)
                 VALUES (?, 'CU11 aceptacion', 'Activo Corriente', 'Débito', 'Activa', 'Todas')
                 """)) {
            ps.setString(1, codigo);
            ps.executeUpdate();
        }

        try (Connection c = conexion();
             PreparedStatement ps = c.prepareStatement("""
                 INSERT INTO AsientosContables
                     (descripcion, debe, haber, tipo, estado, usuario,
                      cuenta_contable, centro_costo, moneda, tipo_cambio,
                      periodo_id, fecha_registro, numero_correlativo)
                 VALUES
                     ('CU11 aceptacion', 500, 0, 'Operación ordinaria',
                      'Activo', 'prueba@finantel.pe', ?, 'General', 'PEN', 1.0,
                      1, GETDATE(), ?)
                 """)) {
            ps.setString(1, codigo);
            ps.setString(2, correlativo);
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
        try (Connection c = conexion();
             PreparedStatement ps = c.prepareStatement(
                 "DELETE FROM CuentasContables WHERE codigo = ?")) {
            ps.setString(1, codigo);
            ps.executeUpdate();
        }
    }

    @Test
    void HA01_la_cuenta_con_saldo_aparece_en_activos() throws Exception {
        mvc.perform(get("/estados-financieros/web/balance")
                .with(user("prueba@finantel.pe")))
            .andExpect(status().isOk())
            .andExpect(content().string(Matchers.containsString(codigo)));
    }

    @Test
    void HA02_el_total_de_activos_refleja_el_saldo_registrado() throws Exception {
        mvc.perform(get("/estados-financieros/web/balance")
                .with(user("prueba@finantel.pe")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalActivo").value(Matchers.greaterThanOrEqualTo(500.0)));
    }
}