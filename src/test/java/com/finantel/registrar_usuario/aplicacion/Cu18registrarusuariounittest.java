package com.finantel.registrar_usuario.aplicacion;

import com.finantel.registrar_accion.aplicacion.AuditoriaService;
import com.finantel.registrar_usuario.dominio.entity.Usuario;
import com.finantel.registrar_usuario.dominio.repository.UsuarioRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CU18RegistrarUsuarioUnitTest {

    @Mock
    private UsuarioRepository repo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private UsuarioService service;

    private Usuario valido() {
        Usuario u = new Usuario();
        u.setNombre("Ana Perez");
        u.setEmail("ana@finantel.pe");
        u.setPassword("clave123");
        u.setRol("Auditor");
        u.setDni("12345678");
        u.setTelefono("987654321");
        return u;
    }

    @Test
    void CP_U1_usuario_valido_se_guarda_activo_con_clave_cifrada() {
        when(repo.findByEmail("ana@finantel.pe")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("clave123")).thenReturn("HASH");

        service.crear(valido());

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(repo).save(captor.capture());
        Usuario guardado = captor.getValue();
        assertEquals("HASH", guardado.getPassword());
        assertEquals("Activo", guardado.getEstado());
        assertEquals(0, guardado.getIntentos());
    }

    @Test
    void CP_U2_crear_registra_la_accion_en_auditoria() {
        when(repo.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("HASH");

        service.crear(valido());

        verify(auditoriaService).registrar(
            eq("ana@finantel.pe"), anyString(), eq("Usuarios"), isNull(), contains("Ana Perez"));
    }

    @Test
    void CP_U3_email_duplicado_lanza_error_y_no_guarda() {
        when(repo.findByEmail("ana@finantel.pe")).thenReturn(Optional.of(new Usuario()));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.crear(valido()));

        assertTrue(ex.getMessage().contains("El email ya"));
        verify(repo, never()).save(any());
    }
}