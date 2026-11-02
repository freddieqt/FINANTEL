package com.finantel.emitir_comprobantes.aplicacion;

import com.finantel.emitir_comprobantes.aplicacion.EmitirComprobantesService;
import com.finantel.emitir_comprobantes.dominio.entity.Comprobante;
import com.finantel.emitir_comprobantes.dominio.repository.EmitirComprobantesRepository;
import com.finantel.registrar_accion.aplicacion.AuditoriaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EmitirComprobantesValidacionesTest {

    @Mock EmitirComprobantesRepository repo;
    @Mock AuditoriaService auditoria;
    @InjectMocks EmitirComprobantesService service;

    private Comprobante comprobante(String numero, String tipo, String cliente, Double monto) {
        Comprobante c = new Comprobante();
        c.setNumero(numero);
        c.setTipo(tipo);
        c.setCliente(cliente);
        c.setMonto(monto);
        return c;
    }

    // ENTRADA: número = "F001-001"; tipo = "Factura"; cliente = "Empresa ABC SAC"; monto = 1500.0
    // RESULTADO: El comprobante se emite sin errores
    @Test
    void EQ_CU_emitir_comprobante_valido_se_emite() {
        assertDoesNotThrow(() -> service.emitir(comprobante("F001-001", "Factura", "Empresa ABC SAC", 1500.0), "ana@finantel.pe"));
    }

    // ENTRADA: número = "F001-001"; tipo = "Factura"; cliente = "" (vacío); monto = 1500.0
    // RESULTADO: Se rechaza con el mensaje "El cliente es obligatorio"
    @Test
    void EQ_CU_emitir_cliente_vacio_se_rechaza() {
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.emitir(comprobante("F001-001", "Factura", "", 1500.0), "ana@finantel.pe"));
        assertTrue(ex.getMessage().contains("El cliente es obligatorio"));
    }

    // ENTRADA: número = "F001-001"; tipo = "Factura"; cliente = "Empresa ABC SAC"; monto = 0.0
    // RESULTADO: Se rechaza con el mensaje "El monto debe ser mayor a 0"
    @Test
    void EQ_CU_emitir_monto_cero_se_rechaza() {
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.emitir(comprobante("F001-001", "Factura", "Empresa ABC SAC", 0.0), "ana@finantel.pe"));
        assertTrue(ex.getMessage().contains("El monto debe ser mayor a 0"));
    }

    // ENTRADA: número = "F001-001"; tipo = null (opción vacía); cliente = "Empresa ABC SAC"; monto = 1500.0
    // RESULTADO: Se rechaza con el mensaje "Tipo inválido"
    @Test
    void EQ_CU_emitir_tipo_vacio_se_rechaza() {
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.emitir(comprobante("F001-001", null, "Empresa ABC SAC", 1500.0), "ana@finantel.pe"));
        assertTrue(ex.getMessage().contains("Tipo inválido"));
    }
}

