package com.finantel.registrar_asiento_contable.integracion;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ConexionBdIT {
    @Test
    void contextoCargaConBaseDeAsientosDePrueba() {
        // Si esta prueba pasa, la aplicación se conecta a FINANTEL_TEST.
    }
}