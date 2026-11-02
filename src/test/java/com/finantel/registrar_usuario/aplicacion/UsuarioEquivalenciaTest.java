package com.finantel.registrar_usuario.aplicacion;

import com.finantel.registrar_accion.aplicacion.AuditoriaService;
import com.finantel.registrar_usuario.aplicacion.UsuarioService;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioEquivalenciaTest {

    @Mock UsuarioRepository repo;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuditoriaService auditoriaService;
    @InjectMocks UsuarioService service;

    private Usuario nuevo(String rol, String password) {
        Usuario u = new Usuario();
        u.setNombre("Usuario de prueba");
        u.setEmail("nuevo@finantel.pe");
        u.setRol(rol);
        u.setPassword(password);
        return u;
    }

    private Usuario existente() {
        Usuario u = new Usuario();
        u.setId(1);
        u.setEmail("ana@finantel.pe");
        u.setPassword("hash-actual");
        return u;
    }

    // ENTRADA: rol = null (opción vacía); contraseña = "123456"; email = nuevo@finantel.pe
    // RESULTADO: Se rechaza con el mensaje "Rol inválido"
    @Test
    void EQ_CU18_rol_vacio_se_rechaza() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.crear(nuevo(null, "123456")));
        assertTrue(ex.getMessage().contains("Rol inválido"));
    }

    // ENTRADA: rol = "Auditor"; contraseña = "12345" (5 caracteres); email = nuevo@finantel.pe
    // RESULTADO: Se rechaza con el mensaje "mínimo 6 caracteres"
    @Test
    void EQ_CU18_contrasena_de_5_caracteres_se_rechaza() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.crear(nuevo("Auditor", "12345")));
        assertTrue(ex.getMessage().contains("6 caracteres"));
    }

    // ENTRADA: rol = "Auditor"; contraseña = "123456" (6 caracteres); email = nuevo@finantel.pe
    // RESULTADO: El usuario se crea sin errores
    @Test
    void EQ_CU18_contrasena_de_6_caracteres_se_acepta() {
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("hash-nuevo");
        assertDoesNotThrow(() -> service.crear(nuevo("Auditor", "123456")));
    }

    // ENTRADA: id = 1; contraseña actual = "" (vacía); contraseña nueva = "123456"
    // RESULTADO: Se rechaza con el mensaje "La contraseña actual es incorrecta"
    @Test
    void EQ_CAMBIAR_contrasena_actual_vacia_se_rechaza() {
        when(repo.findById(1)).thenReturn(Optional.of(existente()));
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.cambiarPassword(1, "", "123456"));
        assertTrue(ex.getMessage().contains("La contraseña actual es incorrecta"));
    }

    // ENTRADA: id = 1; contraseña actual = "clave equivocada"; contraseña nueva = "123456"
    // RESULTADO: Se rechaza con el mensaje "La contraseña actual es incorrecta"
    @Test
    void EQ_CAMBIAR_contrasena_actual_incorrecta_se_rechaza() {
        when(repo.findById(1)).thenReturn(Optional.of(existente()));
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.cambiarPassword(1, "clave equivocada", "123456"));
        assertTrue(ex.getMessage().contains("La contraseña actual es incorrecta"));
    }

    // ENTRADA: id = 1; contraseña actual correcta; contraseña nueva = "12345" (5 caracteres)
    // RESULTADO: Se rechaza con el mensaje "mínimo 6 caracteres"
    @Test
    void EQ_CAMBIAR_contrasena_nueva_de_5_caracteres_se_rechaza() {
        when(repo.findById(1)).thenReturn(Optional.of(existente()));
        lenient().when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.cambiarPassword(1, "clave-actual", "12345"));
        assertTrue(ex.getMessage().contains("mínimo 6 caracteres"));
    }

    // ENTRADA: id = 1; contraseña actual correcta; contraseña nueva = "123456" (6 caracteres)
    // RESULTADO: La contraseña se cambia sin errores
    @Test
    void EQ_CAMBIAR_contrasena_nueva_de_6_caracteres_se_acepta() {
        when(repo.findById(1)).thenReturn(Optional.of(existente()));
        lenient().when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("hash-nuevo");
        assertDoesNotThrow(() -> service.cambiarPassword(1, "clave-actual", "123456"));
    }
}
