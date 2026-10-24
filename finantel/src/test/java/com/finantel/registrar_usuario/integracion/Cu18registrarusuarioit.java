package com.finantel.registrar_usuario.integracion;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CU18RegistrarUsuarioIT {

    private static final String URL_BD =
        "jdbc:sqlserver://localhost:1433;databaseName=FINANTEL_TEST;encrypt=false;trustServerCertificate=true";
    private static final String EMAIL_PRUEBA = "cu18.it@finantel.pe";
    private static final String VALIDO =
        "{\"nombre\":\"CU18 Test\",\"email\":\"cu18.it@finantel.pe\",\"password\":\"clave123\","
        + "\"rol\":\"Auditor\",\"dni\":\"12345678\",\"telefono\":\"987654321\"}";

    @Autowired
    private MockMvc mvc;

    private ResultActions registrar(String json) throws Exception {
        return mvc.perform(post("/web/usuarios")
                .with(user("admin@finantel.pe").authorities(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    @AfterEach
    void limpiar() throws SQLException {
        try (Connection c = DriverManager.getConnection(URL_BD, "sa", "admin098")) {
            try (PreparedStatement ps = c.prepareStatement(
                    "DELETE FROM AuditoriaAcciones WHERE usuario = ?")) {
                ps.setString(1, EMAIL_PRUEBA);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "DELETE FROM Usuarios WHERE email = ?")) {
                ps.setString(1, EMAIL_PRUEBA);
                ps.executeUpdate();
            }
        }
    }

    @Test
    void CP_I1_registrar_usuario_valido_responde_ok() throws Exception {
        registrar(VALIDO)
            .andExpect(status().isOk());
    }

    @Test
    void CP_I2_email_duplicado_responde_400_con_mensaje() throws Exception {
        registrar(VALIDO).andExpect(status().isOk());

        registrar(VALIDO)
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("El email ya")));
    }
}