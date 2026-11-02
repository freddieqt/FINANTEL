package com.finantel.autenticar_usuario.infraestructura.persistence;

import com.finantel.autenticar_usuario.aplicacion.GestorIntentos;
import com.finantel.registrar_usuario.dominio.entity.Usuario;
import com.finantel.registrar_usuario.dominio.repository.UsuarioRepository;

import com.finantel.registrar_accion.aplicacion.AuditoriaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final UsuarioRepository repo;
    private final GestorIntentos gestor;
    private final AuditoriaService auditoriaService;

    public LoginFailureHandler(UsuarioRepository repo, GestorIntentos gestor, AuditoriaService auditoriaService) {
        this.repo = repo;
        this.gestor = gestor;
        this.auditoriaService = auditoriaService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException {
        String email = request.getParameter("username");
        repo.findByEmail(email).ifPresent(gestor::registrarFallo);
        auditoriaService.registrar(
                email != null ? email : "desconocido",
                "LOGIN_FALLIDO",
                "Seguridad",
                "Intento fallido",
                "Intento de inicio de sesión fallido: " + exception.getMessage());
        response.sendRedirect("/login?error=true");
    }
}