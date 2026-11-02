package com.finantel.generar_estados_financieros.presentacion;

import com.finantel.generar_estados_financieros.aplicacion.GenerarEstadosFinancierosService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/estados-financieros")
public class GenerarEstadosFinancierosController {

    private final GenerarEstadosFinancierosService service;

    public GenerarEstadosFinancierosController(GenerarEstadosFinancierosService service) {
        this.service = service;
    }

    @GetMapping("/balance")
    public ResponseEntity<Map<String, Object>> getBalance() {
        return ResponseEntity.ok(service.getBalanceGeneral());
    }
    @GetMapping("/web/balance")
    public ResponseEntity<Map<String, Object>> getBalanceWeb() {
        return ResponseEntity.ok(service.getBalanceGeneralReal());
    }
}