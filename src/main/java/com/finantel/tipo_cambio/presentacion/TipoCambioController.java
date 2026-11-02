package com.finantel.tipo_cambio.presentacion;

import com.finantel.compartido.NegocioException;
import com.finantel.tipo_cambio.aplicacion.TipoCambioSbsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/web/tipo-cambio")
public class TipoCambioController {

    private static final Logger log = LoggerFactory.getLogger(TipoCambioController.class);
    private final TipoCambioSbsService service;

    public TipoCambioController(TipoCambioSbsService service) {
        this.service = service;
    }

    @PostMapping("/actualizar")
    public ResponseEntity<?> actualizar() {
        try {
            return ResponseEntity.ok(service.actualizar());
        } catch (NegocioException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error al actualizar tipo de cambio", e);
            return ResponseEntity.status(500).body(Map.of("error", "Error interno al actualizar el tipo de cambio."));
        }
    }
}
