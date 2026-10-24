package com.finantel.autenticar_usuario.aplicacion;

import com.finantel.registrar_usuario.dominio.entity.Usuario;
import com.finantel.registrar_usuario.dominio.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class GestorIntentos {

    private static final int MAX_INTENTOS = 3;
    private static final Duration BLOQUEO = Duration.ofMinutes(30);

    private final UsuarioRepository repo;

    public GestorIntentos(UsuarioRepository repo) {
        this.repo = repo;
    }

    public boolean estaBloqueado(Usuario u) {
        if (!"Bloqueado".equalsIgnoreCase(u.getEstado())) return false;
        if (u.getFechaBloqueo() != null && LocalDateTime.now().isBefore(u.getFechaBloqueo().plus(BLOQUEO))) {
            return true;
        }
        u.setEstado("Activo");
        u.setIntentos(0);
        u.setFechaBloqueo(null);
        repo.save(u);
        return false;
    }

    public void registrarFallo(Usuario u) {
        int intentos = (u.getIntentos() == null ? 0 : u.getIntentos()) + 1;
        u.setIntentos(intentos);
        if (intentos >= MAX_INTENTOS && !"Bloqueado".equalsIgnoreCase(u.getEstado())) {
            u.setEstado("Bloqueado");
            u.setFechaBloqueo(LocalDateTime.now());
        }
        repo.save(u);
    }

    public void registrarExito(Usuario u) {
        u.setIntentos(0);
        u.setFechaBloqueo(null); // Opcional: limpiar fecha de bloqueo al ingresar con éxito
        repo.save(u);
    }
}