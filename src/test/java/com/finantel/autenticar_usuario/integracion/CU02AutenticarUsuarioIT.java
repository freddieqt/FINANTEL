package com.finantel.autenticar_usuario.integracion;
import com.finantel.registrar_usuario.dominio.entity.Usuario;
import com.finantel.registrar_usuario.dominio.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CU02AutenticarUsuarioIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbc;

    private static final String EMAIL = "itprueba@finantel.pe";
    private static final String PASSWORD = "clave123";

    private void crearUsuario(String estado) {
        Usuario u = new Usuario();
        u.setNombre("Usuario de prueba");
        u.setEmail(EMAIL);
        u.setPassword(passwordEncoder.encode(PASSWORD));
        u.setRol("CONTADOR_GENERAL");
        u.setEstado(estado);
        u.setIntentos(0);
        u.setFechaRegistro(LocalDateTime.now());
        usuarioRepo.saveAndFlush(u);
    }

    private ResultActions login(String email, String password) throws Exception {
        String json = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);
        return mvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json));
    }

    private long contarAuditoria() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM AuditoriaAcciones", Long.class);
    }

    @DisplayName("CP-30 · Usuario y contraseña válidos: autenticación exitosa y auditoría registrada")
    @Test
    void cp30_credencialesValidas_autenticacionExitosa() throws Exception {
        crearUsuario("Activo");
        long auditoriasAntes = contarAuditoria();

        login(EMAIL, PASSWORD)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.rol").value("CONTADOR_GENERAL"));

        assertEquals(auditoriasAntes + 1, contarAuditoria());
    }

    @DisplayName("CP-31 · Usuario inactivo con credenciales correctas: se rechaza (se espera fallo)")
    @Test
    void cp31_usuarioInactivo_seRechaza() throws Exception {
        crearUsuario("Inactivo");

        login(EMAIL, PASSWORD).andExpect(status().isUnauthorized());
    }

    @DisplayName("CP-32 · Contraseña incorrecta: se rechaza con 401")
    @Test
    void cp32_contrasenaIncorrecta_seRechaza() throws Exception {
        crearUsuario("Activo");

        login(EMAIL, "otraClave")
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Credenciales incorrectas"));
    }

    @DisplayName("CP-33 · Usuario que no existe: se rechaza con 401")
    @Test
    void cp33_usuarioInexistente_seRechaza() throws Exception {
        login("noexiste@finantel.pe", PASSWORD)
            .andExpect(status().isUnauthorized());
    }

    @DisplayName("CP-34 · Correo fuera del dominio @finantel.pe: se rechaza con 400")
    @Test
    void cp34_correoFueraDeDominio_seRechaza() throws Exception {
        login("prueba@gmail.com", PASSWORD)
            .andExpect(status().isBadRequest());
    }
}