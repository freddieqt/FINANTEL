package com.finantel.adjuntar_documento.aplicacion;

import com.finantel.adjuntar_documento.dominio.repository.DocumentoAsientoRepository;
import com.finantel.registrar_accion.aplicacion.AuditoriaService;
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
class AdjuntarDocumentoTddTest {

    static final byte[] PDF = {0x25, 0x50, 0x44, 0x46, 0x2D, 0x31, 0x2E, 0x34};

    @Mock
    private DocumentoAsientoRepository repo;

    @Mock
    private AsientoContableRepository asientoRepo;

    @Mock
    private StorageDocumentos storage;

    @Mock
    private AuditoriaService auditoria;

    @InjectMocks
    private DocumentoAsientoService service;

    @Test
    @DisplayName("CP01: No permitir adjuntar documento si el asiento está inactivo")
    void asiento_inactivo_lanza_error() {
        AsientoContable asiento = new AsientoContable();
        asiento.setId(5);
        asiento.setUsuario("contador1");
        asiento.setEstado("INACTIVO"); // ¡Asiento anulado/inactivo!

        when(asientoRepo.findById(5)).thenReturn(Optional.of(asiento));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            service.adjuntar(5, "factura.pdf", PDF, "contador1");
        });

        assertEquals("No se puede adjuntar documentos a un asiento inactivo", ex.getMessage());
    }
}
