package com.finantel.registrar_usuario.aplicacion;

import com.finantel.registrar_accion.aplicacion.AuditoriaService;
import com.finantel.registrar_usuario.dominio.entity.Usuario;
import com.finantel.registrar_usuario.dominio.repository.UsuarioRepository;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CU18RegistrarUsuarioEquivalenciaTest {

    @Mock
    private UsuarioRepository repo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private UsuarioService service;

    private Usuario conRol(String rol) {
        Usuario u = new Usuario();
        u.setNombre("Test");
        u.setEmail("eq@finantel.pe");
        u.setPassword("clave123");
        u.setRol(rol);
        return u;
    }

    @ParameterizedTest
    @ValueSource(strings = {"Administrador", "Contador_General", "Auditor", "Personal_Autorizado"})
    void CP_E1_roles_validos_se_aceptan(String rol) {
        when(repo.findByEmail(anyString())).thenReturn(Optional.empty());
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("HASH");

        assertDoesNotThrow(() -> service.crear(conRol(rol)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"CONTADOR_GENERAL", "Gerente", "administrador"})
    void CP_E2_roles_invalidos_se_rechazan(String rol) {
        when(repo.findByEmail(anyString())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.crear(conRol(rol)));

        assertTrue(ex.getMessage().contains("Rol "));
        verify(repo, never()).save(any());
    }
}