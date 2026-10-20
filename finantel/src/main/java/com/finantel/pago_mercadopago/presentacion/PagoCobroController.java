package com.finantel.pago_mercadopago.presentacion;

import com.finantel.pago_mercadopago.aplicacion.PagoCobroService;
import com.finantel.pago_mercadopago.dominio.entity.PagoMercadoPago;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.net.URI;
@RestController
@RequestMapping("/web/cobros")
public class PagoCobroController {

    private final PagoCobroService cobroService;

    public PagoCobroController(PagoCobroService cobroService) {
        this.cobroService = cobroService;
    }

    @GetMapping("/{asientoId}/enlace")
    public ResponseEntity<Void> generarEnlace(@PathVariable Integer asientoId) throws Exception {
        PagoMercadoPago p = cobroService.generarEnlace(asientoId);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(p.getInitPoint()))
                .build();
    }

    @GetMapping("/{asientoId}/verificar")
    public String verificar(@PathVariable Integer asientoId, Principal principal) throws Exception {
        return cobroService.verificarYRegistrar(asientoId, principal.getName());
    }
}