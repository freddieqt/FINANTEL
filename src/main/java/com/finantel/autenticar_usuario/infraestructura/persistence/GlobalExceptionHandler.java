package com.finantel.autenticar_usuario.infraestructura.persistence;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(com.finantel.compartido.NegocioException.class)
    public ResponseEntity<Map<String, String>> handleNegocio(com.finantel.compartido.NegocioException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleJsonError(org.springframework.http.converter.HttpMessageNotReadableException e,
            jakarta.servlet.http.HttpServletRequest request) {
        String msg = e.getMessage() != null ? e.getMessage() : "";
        String uri = request.getRequestURI();

        if (msg.contains("LocalDate") || msg.contains("fecha"))
            return ResponseEntity.badRequest().body(java.util.Map.of("error",
                    "Formato de fecha inválido. Use: YYYY-MM-DD. Ejemplo: fechaInicio=2026-07-01"));

        if (uri.contains("comprobantes"))
            return ResponseEntity.badRequest().body(java.util.Map.of("error",
                    "El campo 'monto' debe ser un número. Ejemplo: monto=1500.00"));

        if (uri.contains("asientos"))
            return ResponseEntity.badRequest().body(java.util.Map.of("error",
                    "Formato inválido. Los campos debe, haber y tipoCambio deben ser números. Ejemplo: debe=5000, haber=5000, tipoCambio=1.0"));

        return ResponseEntity.badRequest().body(java.util.Map.of("error",
                "Formato de datos inválido. Verifica que los campos numéricos sean números válidos."));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(403)
                .body(Map.of("error", "Acceso denegado — Solo el Administrador puede realizar esta operación"));
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrity(org.springframework.dao.DataIntegrityViolationException e) {
        String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        if (msg.contains("cannot insert the value null") || msg.contains("no permite valores null")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Faltan campos obligatorios para completar el registro."));
        }
        if (msg.contains("comprobantes") || msg.contains("numero")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "El número de comprobante ya se encuentra registrado."));
        }
        if (msg.contains("usuarios") || msg.contains("email")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "El correo electrónico ya se encuentra registrado."));
        }
        if (msg.contains("asientos") || msg.contains("correlativo")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "El número correlativo de asiento ya se encuentra registrado."));
        }
        return ResponseEntity.badRequest()
                .body(Map.of("error", "El registro contiene datos duplicados o viola una restricción de integridad."));
    }

    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    public ResponseEntity<?> handleDatabaseError(org.springframework.dao.DataAccessException e) {
        return ResponseEntity.status(500).body(Map.of(
                "error", "Error interno en la base de datos. Operación no completada."));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        String msg = ex.getMessage();
        if (msg != null && !msg.isBlank()
                && !msg.contains("could not execute statement")
                && !msg.contains("SQL [")
                && !msg.contains("Hibernate")
                && !msg.contains("Exception")
                && !msg.contains("NullPointer")) {
            return ResponseEntity.badRequest().body(Map.of("error", msg));
        }
        org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class).error("Error no controlado", ex);
        return ResponseEntity.status(500).body(Map.of("error", "Ocurrió un error interno. Intente nuevamente."));
    }
}
