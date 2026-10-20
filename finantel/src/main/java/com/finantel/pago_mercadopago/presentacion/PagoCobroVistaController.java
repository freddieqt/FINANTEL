package com.finantel.pago_mercadopago.presentacion;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PagoCobroVistaController {

    @GetMapping("/web/cobros/{asientoId}")
    public String mostrar(@PathVariable Integer asientoId, Model model) {
        model.addAttribute("asientoId", asientoId);
        return "cobros";
    }
}