package com.finantel.registrar_usuario.aceptacion;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CU18RegistrarUsuarioAceptacionTest {

    private static final String URL_BD =
        "jdbc:sqlserver://localhost:1433;databaseName=FINANTEL_TEST;encrypt=false;trustServerCertificate=true";
    private static final String EMAIL_PRUEBA = "cu18.ha@finantel.pe";
    private static final String JSON =
        "{\"nombre\":\"CU18 Aceptacion\",\"email\":\"cu18.ha@finantel.pe\",\"password\":\"clave123\","
        + "\"rol\":\"Auditor\",\"dni\":\"12345678\",\"telefono\":\"987654321\"}";

    @Autowired
    private MockMvc mvc;

    private void registrar() throws Exception {
        mvc.perform(post("/web/usuarios")
                .with(user("admin@finantel.pe").authorities(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON));
    }

    private static Connection conexion() throws SQLException {
        return DriverManager.getConnection(URL_BD, "sa", "admin098");
    }

    private int contarUsuarios() throws SQLException {
        try (Connection c = conexion();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT COUNT(*) FROM Usuarios WHERE email = ? AND estado = 'Activo'")) {
            ps.setString(1, EMAIL_PRUEBA);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    @AfterEach
    void limpiar() throws SQLException {
        try (Connection c = conexion()) {
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM AuditoriaAcciones WHERE usuario = ?")) {
                ps.setString(1, EMAIL_PRUEBA);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM Usuarios WHERE email = ?")) {
                ps.setString(1, EMAIL_PRUEBA);
                ps.executeUpdate();
            }
        }
    }

    @Test
    void HA01_el_usuario_nuevo_queda_registrado_como_activo() throws Exception {
        registrar();

        assertEquals(1, contarUsuarios());
    }

    @Test
    void HA02_no_se_crean_usuarios_duplicados() throws Exception {
        registrar();
        registrar();

        assertEquals(1, contarUsuarios());
    }
}