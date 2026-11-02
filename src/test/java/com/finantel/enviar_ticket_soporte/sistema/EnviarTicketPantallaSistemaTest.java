package com.finantel.enviar_ticket_soporte.sistema;

import com.finantel.pantalla.PantallaBase;
import org.junit.jupiter.api.Test;

public class EnviarTicketPantallaSistemaTest extends PantallaBase {

    private void abrirTicket() {
        iniciarComoContador();
        page.evaluate("abrirTicketSoporte()");
        page.waitForSelector("#ticket-asunto");
    }

    private void enviar() {
        page.click("button[onclick='enviarTicket()']");
    }

    // ENTRADA: [Enviar ticket pantalla] asunto = "" (vacío); descripción = "Descripción de prueba"
    // RESULTADO: Se muestra el mensaje "El asunto es obligatorio"
    @Test
    void asunto_vacio_muestra_error() {
        abrirTicket();
        page.fill("#ticket-asunto", "");
        page.fill("#ticket-desc", "Descripción de prueba");
        enviar();
        assertToastContiene("El asunto es obligatorio");
    }

    // ENTRADA: [Enviar ticket pantalla] asunto = "Error"; descripción = "" (vacía)
    // RESULTADO: Se muestra el mensaje "La descripción es obligatoria"
    @Test
    void descripcion_vacia_muestra_error() {
        abrirTicket();
        page.fill("#ticket-asunto", "Error");
        page.fill("#ticket-desc", "");
        enviar();
        assertToastContiene("La descripción es obligatoria");
    }
}
