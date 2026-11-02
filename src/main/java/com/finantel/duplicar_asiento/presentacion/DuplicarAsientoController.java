package com.finantel.duplicar_asiento.presentacion;

import com.finantel.compartido.NegocioException;
import com.finantel.duplicar_asiento.aplicacion.DuplicarAsientoService;
import com.finantel.duplicar_asiento.infraestructura.mapping.PrototypeAsiento;
import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import com.finantel.registrar_asiento_contable.dominio.repository.AsientoContableRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.jspecify.annotations.NonNull;
import java.security.Principal;

@RestController
@RequestMapping("/duplicar-asiento")
public class DuplicarAsientoController {

    private static final Logger log = LoggerFactory.getLogger(DuplicarAsientoController.class);
    private final DuplicarAsientoService service;
    private final AsientoContableRepository repo;

    public DuplicarAsientoController(DuplicarAsientoService service,
            AsientoContableRepository repo) {
        this.service = service;
        this.repo = repo;
    }

    @PostMapping("/{id}")
    public ResponseEntity<?> duplicar(@PathVariable("id") @NonNull Integer id, Principal principal) {
        service.duplicar(id, principal.getName());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/prototype/{id}")
    public ResponseEntity<String> prototipo(@PathVariable(name = "id") @NonNull Integer id) {
        try {
            AsientoContable original = repo.findById(id)
                .orElseThrow(() -> new NegocioException("Asiento no encontrado: " + id));

            PrototypeAsiento prototipo = new PrototypeAsiento(
                original.getNumeroCorrelativo(),
                original.getTipo(),
                original.getDescripcion(),
                original.getDebe(),
                original.getHaber(),
                original.getCuentaContable(),
                original.getUsuario(),
                original.getFechaRegistro() != null ? original.getFechaRegistro().toString() : "Sin fecha"
            );

            prototipo.clonar("AS-CLON-" + id);

            return ResponseEntity.ok("Operación completada");
        } catch (NegocioException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error al generar prototipo de asiento", e);
            return ResponseEntity.status(500).body("Error interno al duplicar el asiento.");
        }
    }
}