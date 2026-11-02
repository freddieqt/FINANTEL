package com.finantel.gestionar_periodos.presentacion;

import com.finantel.compartido.NegocioException;
import com.finantel.gestionar_periodos.aplicacion.PeriodoContableService;
import com.finantel.gestionar_periodos.dominio.entity.PeriodoContable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.jspecify.annotations.NonNull;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/periodos-contables")
public class PeriodoContableController {

    private static final Logger log = LoggerFactory.getLogger(PeriodoContableController.class);
    private final PeriodoContableService service;

    public PeriodoContableController(PeriodoContableService service) {
        this.service = service;
    }

    @GetMapping
    public List<PeriodoContable> listar() {
        return service.listar();
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody PeriodoContable p) {
        try {
            service.crear(p);
            return ResponseEntity
                    .ok(Map.of("mensaje", "Periodo '" + p.getPeriodo() + "' creado correctamente"));
        } catch (NegocioException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error al crear periodo contable", e);
            return ResponseEntity.status(500).body(Map.of("error", "Error interno al crear el periodo."));
        }
    }

    @PatchMapping("/{id}/cerrar")
    public ResponseEntity<?> cerrar(@PathVariable("id") @NonNull Integer id, Principal principal) {
        try {
            service.cerrar(id, principal.getName());
            return ResponseEntity.ok(Map.of("mensaje", "Periodo cerrado correctamente"));
        } catch (NegocioException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error al cerrar periodo contable {}", id, e);
            return ResponseEntity.status(500).body(Map.of("error", "Error interno al cerrar el periodo."));
        }
    }
}
