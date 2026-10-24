package com.finantel.registrar_usuario.aplicacion;

import com.finantel.registrar_accion.aplicacion.AuditoriaService;
import com.finantel.registrar_usuario.dominio.entity.Usuario;
import com.finantel.registrar_usuario.dominio.repository.UsuarioRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CU18RegistrarUsuarioLimitesTest {

    @Mock
    private UsuarioRepository repo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private UsuarioService service;

    private void crear(String clave, String dni, String telefono) {
        Usuario u = new Usuario();
        u.setNombre("Test");
        u.setEmail("lim@finantel.pe");
        u.setPassword(clave);
        u.setRol("Auditor");
        u.setDni(dni);
        u.setTelefono(telefono);
        service.crear(u);
    }

    private void prepararRepositorio() {
        when(repo.findByEmail(anyString())).thenReturn(Optional.empty());
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("HASH");
    }

    @Test
    void CP_L1_clave_de_6_caracteres_debe_ser_aceptada() {
        prepararRepositorio();
        assertDoesNotThrow(() -> crear("123456", "12345678", "987654321"));
    }

    @Test
    void CP_L2_clave_de_7_caracteres_debe_ser_aceptada() {
        prepararRepositorio();
        assertDoesNotThrow(() -> crear("1234567", "12345678", "987654321"));
    }

    @Test
    void CP_L3_dni_de_7_digitos_se_rechaza() {
        prepararRepositorio();
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> crear("clave123", "1234567", "987654321"));
        assertTrue(ex.getMessage().contains("8 numeros"));
    }

    @Test
    void CP_L4_dni_de_8_digitos_se_acepta() {
        prepararRepositorio();
        assertDoesNotThrow(() -> crear("clave123", "12345678", "987654321"));
    }

    @Test
    void CP_L5_telefono_que_empieza_en_8_se_rechaza() {
        prepararRepositorio();
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> crear("clave123", "12345678", "887654321"));
        assertTrue(ex.getMessage().contains("debe tener 9"));
    }

    @Test
    void CP_L6_telefono_de_9_digitos_que_empieza_en_9_se_acepta() {
        prepararRepositorio();
        assertDoesNotThrow(() -> crear("clave123", "12345678", "987654321"));
    }
}