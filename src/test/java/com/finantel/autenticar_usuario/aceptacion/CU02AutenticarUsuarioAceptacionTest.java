package com.finantel.autenticar_usuario.aceptacion;

import com.finantel.registrar_usuario.dominio.entity.Usuario;
import com.finantel.registrar_usuario.dominio.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CU02AutenticarUsuarioAceptacionTest {

    private static final String EMAIL = "ha.prueba@finantel.pe";
    private static final String CLAVE = "clave123";

    @Autowired private MockMvc mvc;
    @Autowired private UsuarioRepository repo;
    @Autowired private PasswordEncoder encoder;
    @Autowired private JdbcTemplate jdbc;

    private void crearUsuario() {
        Usuario u = new Usuario();
        u.setNombre("HA Prueba");
        u.setEmail(EMAIL);
        u.setPassword(encoder.encode(CLAVE));
        u.setRol("CONTADOR_GENERAL");
        u.setEstado("Activo");
        u.setIntentos(0);
        u.setFechaRegistro(LocalDateTime.now());
        repo.save(u);
    }

    private int login(String clave) throws Exception {
        String json = "{\"email\":\"" + EMAIL + "\",\"password\":\"" + clave + "\"}";
        return mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andReturn().getResponse().getStatus();
    }

    private int contarAuditoria() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM AuditoriaAcciones", Integer.class);
    }

    @Test
    void HA01_usuario_valido_ingresa_al_sistema() throws Exception {
        crearUsuario();
        mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + EMAIL + "\",\"password\":\"" + CLAVE + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void HA02_contrasena_guardada_cifrada_con_bcrypt() {
        crearUsuario();
        String hash = jdbc.queryForObject(
            "SELECT password FROM Usuarios WHERE email = ?", String.class, EMAIL);
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$"));
        assertNotEquals(CLAVE, hash);
    }

    @Test
    void HA03_tres_fallos_bloquean_aunque_la_clave_sea_correcta() throws Exception {
        crearUsuario();
        for (int i = 0; i < 3; i++) {
            assertEquals(401, login("mal12345"));
        }
        assertEquals(423, login(CLAVE));
    }

    @Test
    void HA04_login_queda_registrado_en_auditoria() throws Exception {
        crearUsuario();
        int antes = contarAuditoria();
        assertEquals(200, login(CLAVE));
        assertEquals(antes + 1, contarAuditoria());
    }
}