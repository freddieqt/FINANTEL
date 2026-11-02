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
class PagoMercadoPagoLimitesTest {

    @Mock
    private PagoService pagoService;

    @Mock
    private PagoMercadoPagoRepository pagoRepo;

    @Mock
    private AsientoContableRepository asientoRepo;

    @InjectMocks
    private PagoCobroService service;

    @Test
    @DisplayName("BVA-01: Límite inferior inválido - Monto 0.00 es rechazado")
    void monto_cero_limite_invalido() {
        AsientoContable a = new AsientoContable();
        a.setId(1);
        a.setCuentaContable("1201");
        a.setDebe(0.00); // Límite inferior inválido
        when(asientoRepo.findById(1)).thenReturn(Optional.of(a));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.generarEnlace(1));
        assertEquals("El monto debe ser mayor a cero", ex.getMessage());
    }

    @Test
    @DisplayName("BVA-02: Límite inferior válido - Monto mínimo de 0.01 es aceptado")
    void monto_un_centimo_limite_valido() throws Exception {
        AsientoContable a = new AsientoContable();
        a.setId(2);
        a.setCuentaContable("1201");
        a.setDebe(0.01); // Límite mínimo válido
        a.setDescripcion("Cobro mínimo");
        a.setNumeroCorrelativo("AS-MIN");

        when(asientoRepo.findById(2)).thenReturn(Optional.of(a));
        when(pagoService.crearEnlace(anyString(), any(BigDecimal.class), anyString()))
                .thenReturn("https://mercadopago.com/checkout/min");
        when(pagoRepo.save(any(PagoMercadoPago.class))).thenAnswer(inv -> inv.getArgument(0));

        PagoMercadoPago res = service.generarEnlace(2);
        assertNotNull(res);
        assertEquals(new BigDecimal("0.01"), res.getMonto());
    }

    @Test
    @DisplayName("BVA-03: Límite superior válido - Monto de 500,000.00 es procesado")
    void monto_maximo_limite_valido() throws Exception {
        AsientoContable a = new AsientoContable();
        a.setId(3);
        a.setCuentaContable("1201");
        a.setDebe(500000.00);
        a.setDescripcion("Cobro corporativo");
        a.setNumeroCorrelativo("AS-MAX");

        when(asientoRepo.findById(3)).thenReturn(Optional.of(a));
        when(pagoService.crearEnlace(anyString(), any(BigDecimal.class), anyString()))
                .thenReturn("https://mercadopago.com/checkout/max");
        when(pagoRepo.save(any(PagoMercadoPago.class))).thenAnswer(inv -> inv.getArgument(0));

        PagoMercadoPago res = service.generarEnlace(3);
        assertNotNull(res);
        assertEquals(new BigDecimal("500000.0"), res.getMonto());
    }
}
