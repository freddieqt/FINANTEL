package com.finantel.emitir_comprobantes.aplicacion;

import com.finantel.emitir_comprobantes.dominio.entity.Comprobante;
import com.finantel.emitir_comprobantes.dominio.repository.EmitirComprobantesRepository;
import com.finantel.emitir_comprobantes.infraestructura.persistence.EmitirComprobantesConfig;
import com.finantel.registrar_accion.aplicacion.AuditoriaService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmitirComprobantesLimitesTest {

    private final EmitirComprobantesConfig config = new EmitirComprobantesConfig();

    @Mock
    private EmitirComprobantesRepository repo;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private EmitirComprobantesService service;

    private Comprobante conMonto(Double monto) {
        Comprobante c = new Comprobante();
        c.setNumero("F001-001");
        c.setTipo("Factura");
        c.setCliente("Empresa ABC SAC");
        c.setMonto(monto);
        return c;
    }

    @Test
    void CP_L1_monto_igual_a_cero_se_rechaza() {
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.emitir(conMonto(0.0), "admin@finantel.pe"));
        assertTrue(ex.getMessage().contains("mayor a 0"));
        verify(repo, never()).save(any());
    }

    @Test
    void CP_L2_monto_de_0_01_se_acepta() {
        assertDoesNotThrow(() -> service.emitir(conMonto(0.01), "admin@finantel.pe"));
    }

    @Test
    void CP_L3_monto_nulo_se_rechaza() {
        assertThrows(RuntimeException.class,
            () -> service.emitir(conMonto(null), "admin@finantel.pe"));
    }

    @Test
    void CP_L4_numero_en_blanco_se_rechaza() {
        Comprobante c = conMonto(100.0);
        c.setNumero("   ");

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.emitir(c, "admin@finantel.pe"));
        assertTrue(ex.getMessage().contains("obligatorio"));
    }

    @Test
    void CP_L5_cliente_en_blanco_se_rechaza() {
        Comprobante c = conMonto(100.0);
        c.setCliente("   ");

        assertThrows(RuntimeException.class,
            () -> service.emitir(c, "admin@finantel.pe"));
    }

    @Test
    void CP_L6_config_rechaza_monto_nulo_y_negativo() {
        assertFalse(config.esMontoValido(null));
        assertFalse(config.esMontoValido(-0.01));
    }
}