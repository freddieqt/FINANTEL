package com.finantel.emitir_comprobantes.aplicacion;

import com.finantel.emitir_comprobantes.dominio.entity.Comprobante;
import com.finantel.emitir_comprobantes.dominio.repository.EmitirComprobantesRepository;
import com.finantel.registrar_accion.aplicacion.AuditoriaService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmitirComprobantesEquivalenciaTest {

    @Mock
    private EmitirComprobantesRepository repo;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private EmitirComprobantesService service;

    private Comprobante conTipo(String tipo) {
        Comprobante c = new Comprobante();
        c.setNumero("F001-001");
        c.setTipo(tipo);
        c.setCliente("Empresa ABC SAC");
        c.setMonto(1500.0);
        return c;
    }

    @ParameterizedTest
    @ValueSource(strings = {"Factura", "Boleta", "Recibo de Pago"})
    void CP_E1_tipos_validos_se_emiten(String tipo) {
        assertDoesNotThrow(() -> service.emitir(conTipo(tipo), "admin@finantel.pe"));
        verify(repo).save(any(Comprobante.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Ticket", "factura", "Nota"})
    void CP_E2_tipos_no_validos_se_rechazan(String tipo) {
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.emitir(conTipo(tipo), "admin@finantel.pe"));
        assertTrue(ex.getMessage().contains("Tipo"));
        verify(repo, never()).save(any());
    }
}