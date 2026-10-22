package com.finantel.autenticar_usuario.sistema;

import com.finantel.registrar_usuario.dominio.entity.Usuario;
import com.finantel.registrar_usuario.dominio.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

@SpringBootTest
@ActiveProfiles("test")
class CrearUsuariosPruebaTest {

    @Autowired
    private UsuarioRepository repo;

    @Autowired
    private PasswordEncoder encoder;

    @Test
    void crearUsuariosDePrueba() {
        crear("prueba@finantel.pe", "Activo");
        crear("inactivo@finantel.pe", "Inactivo");
    }

    private void crear(String email, String estado) {
        if (repo.findByEmail(email).isPresent()) return;
        Usuario u = new Usuario();
        u.setNombre("Usuario de prueba");
        u.setEmail(email);
        u.setPassword(encoder.encode("clave123"));
        u.setRol("CONTADOR_GENERAL");
        u.setEstado(estado);
        u.setIntentos(0);
        u.setFechaRegistro(LocalDateTime.now());
        repo.saveAndFlush(u);
    }
}