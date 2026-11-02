package com.finantel.inactivar_asiento.sistema;

import com.finantel.pantalla.PantallaBase;
import org.junit.jupiter.api.Test;

// ENTRADA: [CU05 pantalla] asiento id = 1; motivo de anulación = "" (vacío)
// RESULTADO: Se muestra el mensaje "Ingresa el motivo de anulación" y no se anula el asiento
public class AnularAsientoMotivoPantallaSistemaTest extends PantallaBase {

    @Test
    void motivo_vacio_muestra_error() {
        iniciarComoContador();
        page.evaluate("anularAsiento(1)");
        page.waitForSelector("#motivo-anular");
        page.fill("#motivo-anular", "");
        page.click("text=🗑 Confirmar Anulación");
        assertToastContiene("Ingresa el motivo de anulación");
    }
}
