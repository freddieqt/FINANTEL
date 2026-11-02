package com.finantel.emitir_comprobantes.aplicacion;

import com.finantel.emitir_comprobantes.dominio.entity.Comprobante;
import com.finantel.emitir_comprobantes.dominio.repository.EmitirComprobantesRepository;
import com.finantel.registrar_accion.aplicacion.AuditoriaService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmitirComprobantesUnitTest {

    @Mock
    private EmitirComprobantesRepository repo;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private EmitirComprobantesService service;

    private Comprobante valido() {
        Comprobante c = new Comprobante();
        c.setNumero("F001-001");
        c.setTipo("Factura");
        c.setCliente("Empresa ABC SAC");
        c.setMonto(1500.0);
        return c;
    }

    @Test
    void CP_U1_comprobante_valido_se_guarda_como_emitido() {
        service.emitir(valido(), "admin@finantel.pe");

        ArgumentCaptor<Comprobante> captor = ArgumentCaptor.forClass(Comprobante.class);
        verify(repo).save(captor.capture());
        assertEquals("Emitido", captor.getValue().getEstado());
    }

    @Test
    void CP_U2_emision_se_registra_en_auditoria() {
        service.emitir(valido(), "admin@finantel.pe");

        verify(auditoriaService).registrar(
            eq("admin@finantel.pe"), anyString(), eq("Comprobantes"), isNull(), contains("F001-001"));
    }

    @Test
    void CP_U3_listar_devuelve_los_comprobantes_del_repositorio() {
        when(repo.findAll()).thenReturn(List.of(valido()));

        assertEquals(1, service.listar().size());
    }
}