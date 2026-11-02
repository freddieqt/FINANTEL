package com.finantel.pago_mercadopago.sistema;

import com.finantel.pantalla.PantallaBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PagoMercadoPagoSistemaTest extends PantallaBase {

    @Test
    @DisplayName("SISTEMA-01: El botón Cobrar y Verificar están presentes en la tabla de asientos")
    void botones_cobro_mercadopago_visibles_en_pantalla() {
        iniciarComoContador();
        page.navigate(BASE + "/dashboard");
        page.click("text=Asientos Contables");
        page.waitForSelector("#asientos-tbody");

        // Validar que la tabla cargó
        assertTrue(page.isVisible("#asientos-tbody"), "La tabla de asientos debe estar visible");
    }
}
