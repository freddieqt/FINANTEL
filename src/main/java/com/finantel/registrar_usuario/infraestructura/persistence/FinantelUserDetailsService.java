package com.finantel.registrar_usuario.infraestructura.persistence;

import com.finantel.autenticar_usuario.aplicacion.GestorIntentos;
import com.finantel.registrar_usuario.dominio.entity.Usuario;
import com.finantel.registrar_usuario.dominio.repository.UsuarioRepository;

import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FinantelUserDetailsService implements UserDetailsService {

    private final UsuarioRepository repo;
    private final GestorIntentos gestor;

    public FinantelUserDetailsService(UsuarioRepository repo, GestorIntentos gestor) {
        this.repo = repo;
        this.gestor = gestor;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario u = repo.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        // Bloqueo temporal (30 min): si ya pasó el tiempo, lo desbloquea
        if (gestor.estaBloqueado(u)) {
            throw new LockedException("Cuenta bloqueada temporalmente");
        }

        // Bloquear si está inactivo
        if (u.getEstado() == null || !u.getEstado().equalsIgnoreCase("activo")) {
            throw new UsernameNotFoundException("Usuario inactivo: " + email);
        }

        return new org.springframework.security.core.userdetails.User(
                u.getEmail(),
                u.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + u.getRol().toUpperCase().replace(" ", "_")))
            );
    }
}