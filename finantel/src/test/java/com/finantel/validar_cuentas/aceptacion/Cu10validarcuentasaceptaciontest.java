package com.finantel.validar_cuentas.aceptacion;

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
class CU10ValidarCuentasAceptacionTest {

    private static final String URL_BD =
        "jdbc:sqlserver://localhost:1433;databaseName=FINANTEL_TEST;encrypt=false;trustServerCertificate=true";

    @Autowired
    private MockMvc mvc;

    private String codigo;

    private static Connection conexion() throws SQLException {
        return DriverManager.getConnection(URL_BD, "sa", "admin098");
    }

    @BeforeEach
    void preparar() throws SQLException {
        codigo = "U" + (System.nanoTime() % 100000);

        String sql = """
            INSERT INTO CuentasContables
                (codigo, descripcion, clasificacion, tipo, estado, ops_autorizadas)
            VALUES
                (?, 'CU10 aceptacion', 'Activo Corriente', 'Débito', 'Activa', 'Todas')
            """;

        try (Connection c = conexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, codigo);
            ps.executeUpdate();
        }
    }

    @AfterEach
    void limpiar() throws SQLException {
        try (Connection c = conexion();
             PreparedStatement ps = c.prepareStatement(
                 "DELETE FROM CuentasContables WHERE codigo = ?")) {
            ps.setString(1, codigo);
            ps.executeUpdate();
        }
    }

    @Test
    void HA01_una_cuenta_activa_registrada_se_reporta_como_valida() throws Exception {
        mvc.perform(get("/validar-cuentas/" + codigo)
                .with(user("prueba@finantel.pe")))
            .andExpect(status().isOk())
            .andExpect(content().string(Matchers.containsString("Cuenta válida y activa")));
    }

    @Test
    void HA02_un_codigo_no_registrado_se_reporta_como_no_encontrado() throws Exception {
        mvc.perform(get("/validar-cuentas/NOEXISTE999")
                .with(user("prueba@finantel.pe")))
            .andExpect(status().isOk())
            .andExpect(content().string(Matchers.containsString("Cuenta no encontrada")));
    }
}