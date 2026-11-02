package com.finantel.pago_mercadopago.aplicacion;

import com.finantel.pago_mercadopago.dominio.entity.PagoMercadoPago;
import com.finantel.pago_mercadopago.dominio.repository.PagoMercadoPagoRepository;
import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import com.finantel.registrar_asiento_contable.dominio.repository.AsientoContableRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoMercadoPagoUnitTest {

    @Mock
    private PagoService pagoService;

    @Mock
    private PagoMercadoPagoRepository pagoRepo;

    @Mock
    private AsientoContableRepository asientoRepo;

    @InjectMocks
    private PagoCobroService service;

    @Test
    @DisplayName("UNIT-01: Asiento no existente lanza excepción")
    void asiento_no_existente_lanza_error() {
        when(asientoRepo.findById(999)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            service.generarEnlace(999);
        });
        assertEquals("Asiento no encontrado", ex.getMessage());
    }

    @Test
    @DisplayName("UNIT-02: Cuenta no cobrable distinta a 1201 lanza excepción")
    void cuenta_no_cobrable_lanza_error() {
        AsientoContable a = new AsientoContable();
        a.setId(1);
        a.setCuentaContable("1011");
        when(asientoRepo.findById(1)).thenReturn(Optional.of(a));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            service.generarEnlace(1);
        });
        assertTrue(ex.getMessage().contains("Solo se pueden cobrar asientos de la cuenta 1201"));
    }

    @Test
    @DisplayName("UNIT-03: Generación de enlace con cuenta 1201 guarda registro en estado PENDIENTE")
    void generacion_exitosa_guarda_estado_pendiente() throws Exception {
        AsientoContable a = new AsientoContable();
        a.setId(10);
        a.setCuentaContable("1201");
        a.setDebe(100.0);
        a.setDescripcion("Venta");
        a.setNumeroCorrelativo("REC-001");

        when(asientoRepo.findById(10)).thenReturn(Optional.of(a));
        when(pagoService.crearEnlace(anyString(), any(BigDecimal.class), anyString()))
                .thenReturn("https://mercadopago.com/checkout/123");
        when(pagoRepo.save(any(PagoMercadoPago.class))).thenAnswer(inv -> inv.getArgument(0));

        PagoMercadoPago res = service.generarEnlace(10);

        assertNotNull(res);
        assertEquals("PENDIENTE", res.getEstado());
        assertEquals("REC-001", res.getNumeroCorrelativo());
    }
}
