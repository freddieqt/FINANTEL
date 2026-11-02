package com.finantel.autenticar_usuario.infraestructura.jwt;

import com.finantel.autenticar_usuario.aplicacion.LoginRequest;
import com.finantel.autenticar_usuario.aplicacion.GestorIntentos;
import com.finantel.autenticar_usuario.infraestructura.persistence.SingletonConfiguracionSistema;
import com.finantel.registrar_accion.aplicacion.AuditoriaService;
import com.finantel.registrar_usuario.dominio.entity.Usuario;
import com.finantel.registrar_usuario.dominio.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class JwtAuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuditoriaService auditoriaService;
    private final GestorIntentos gestor;

    public JwtAuthController(UsuarioRepository usuarioRepository,
                             PasswordEncoder passwordEncoder,
                             JwtUtil jwtUtil,
                             AuditoriaService auditoriaService,
                             GestorIntentos gestor) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.auditoriaService = auditoriaService;
        this.gestor = gestor;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        
        if (usuario != null && gestor.estaBloqueado(usuario)) {
            return ResponseEntity.status(423).body(Map.of("error", "Cuenta bloqueada temporalmente"));
        }
        
        if (usuario == null || !passwordEncoder.matches(password, usuario.getPassword())
                || !"Activo".equalsIgnoreCase(usuario.getEstado())) {
            if (usuario != null && !passwordEncoder.matches(password, usuario.getPassword())) {
                gestor.registrarFallo(usuario);
            }
            auditoriaService.registrar(email, "Inicio de sesión fallido", "Sistema", null, "Credenciales incorrectas");
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales incorrectas"));
        }
        
        gestor.registrarExito(usuario);
        String token = jwtUtil.generateToken(email, usuario.getRol());
        auditoriaService.registrar(email, "Inicio de sesión", "Sistema", null, "Acceso exitoso - Rol: " + usuario.getRol());
        SingletonConfiguracionSistema.getInstancia().incrementarSesiones();
        
        return ResponseEntity.ok(Map.of(
            "token", token,
            "email", email,
            "rol", usuario.getRol()
        ));
    }
}