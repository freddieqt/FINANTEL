package com.finantel.pago_mercadopago.aplicacion;

import com.finantel.pago_mercadopago.dominio.repository.PagoMercadoPagoRepository;
import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import com.finantel.registrar_asiento_contable.dominio.repository.AsientoContableRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoMercadoPagoTddTest {

    @Mock
    private PagoService pagoService;

    @Mock
    private PagoMercadoPagoRepository pagoRepo;

    @Mock
    private AsientoContableRepository asientoRepo;

    @InjectMocks
    private PagoCobroService service;

    @Test
    @DisplayName("CP01: No permitir cobro con monto menor o igual a cero")
    void monto_cero_o_negativo_lanza_error() {
        AsientoContable asiento = new AsientoContable();
        asiento.setId(99);
        asiento.setCuentaContable("1201");
        asiento.setDebe(0.0); // ¡Monto en Cero!
        asiento.setNumeroCorrelativo("AS-099");
        asiento.setDescripcion("Asiento sin monto");

        when(asientoRepo.findById(99)).thenReturn(Optional.of(asiento));

        // Esperamos que el servicio rechace cobrar 0 soles:
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            service.generarEnlace(99);
        });

        assertEquals("El monto debe ser mayor a cero", ex.getMessage());
    }
}
