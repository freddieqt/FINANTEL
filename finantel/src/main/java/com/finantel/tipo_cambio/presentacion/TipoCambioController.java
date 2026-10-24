package com.finantel.tipo_cambio.presentacion;

import com.finantel.tipo_cambio.aplicacion.TipoCambioSbsService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/web/tipo-cambio")
public class TipoCambioController {

    private final TipoCambioSbsService service;

    public TipoCambioController(TipoCambioSbsService service) {
        this.service = service;
    }

    @PostMapping("/actualizar")
    public ResponseEntity<?> actualizar() {
        try {
            return ResponseEntity.ok(service.actualizar());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}