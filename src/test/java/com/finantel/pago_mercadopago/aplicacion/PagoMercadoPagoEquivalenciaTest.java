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
class PagoMercadoPagoEquivalenciaTest {

    @Mock
    private PagoService pagoService;

    @Mock
    private PagoMercadoPagoRepository pagoRepo;

    @Mock
    private AsientoContableRepository asientoRepo;

    @InjectMocks
    private PagoCobroService service;

    @Test
    @DisplayName("EQ-01: Clase Válida - Cuenta por cobrar 1201 con monto positivo se acepta")
    void EQ_cuenta_1201_valida_se_acepta() throws Exception {
        AsientoContable a = new AsientoContable();
        a.setId(1);
        a.setCuentaContable("1201");
        a.setDebe(150.0);
        a.setDescripcion("Factura 001");
        a.setNumeroCorrelativo("FAC-001");

        when(asientoRepo.findById(1)).thenReturn(Optional.of(a));
        when(pagoService.crearEnlace(anyString(), any(BigDecimal.class), anyString()))
                .thenReturn("https://mercadopago.com/init");
        when(pagoRepo.save(any(PagoMercadoPago.class))).thenAnswer(inv -> inv.getArgument(0));

        PagoMercadoPago res = service.generarEnlace(1);
        assertNotNull(res);
        assertEquals("PENDIENTE", res.getEstado());
    }

    @Test
    @DisplayName("EQ-02: Clase Inválida - Cuenta que no es por cobrar (ej. 4212 proveedores) se rechaza")
    void EQ_cuenta_proveedores_invalida_se_rechaza() {
        AsientoContable a = new AsientoContable();
        a.setId(2);
        a.setCuentaContable("4212");
        when(asientoRepo.findById(2)).thenReturn(Optional.of(a));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.generarEnlace(2));
        assertTrue(ex.getMessage().contains("Solo se pueden cobrar asientos de la cuenta 1201"));
    }

    @Test
    @DisplayName("EQ-03: Clase Inválida - Monto negativo se rechaza")
    void EQ_monto_negativo_invalido_se_rechaza() {
        AsientoContable a = new AsientoContable();
        a.setId(3);
        a.setCuentaContable("1201");
        a.setDebe(-50.0);
        when(asientoRepo.findById(3)).thenReturn(Optional.of(a));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.generarEnlace(3));
        assertEquals("El monto debe ser mayor a cero", ex.getMessage());
    }
}
