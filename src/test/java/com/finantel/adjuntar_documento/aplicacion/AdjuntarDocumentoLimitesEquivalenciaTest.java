package com.finantel.adjuntar_documento.aplicacion;

import com.finantel.adjuntar_documento.dominio.entity.DocumentoAsiento;
import com.finantel.adjuntar_documento.dominio.repository.DocumentoAsientoRepository;
import com.finantel.registrar_accion.aplicacion.AuditoriaService;
import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import com.finantel.registrar_asiento_contable.dominio.repository.AsientoContableRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdjuntarDocumentoLimitesEquivalenciaTest {

    static final byte[] PDF = {0x25, 0x50, 0x44, 0x46, 0x2D, 0x31, 0x2E, 0x34};
    static final byte[] PNG = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    static final byte[] JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0};
    static final byte[] ZIP_DOCX = {0x50, 0x4B, 0x03, 0x04};

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

    private void asientoDeAna() {
        AsientoContable a = new AsientoContable();
        a.setUsuario("ana@finantel.pe");
        lenient().when(asientoRepo.findById(1)).thenReturn(Optional.of(a));
        lenient().when(storage.guardar(any(), anyString())).thenReturn("ruta/uuid");
        lenient().when(repo.save(any(DocumentoAsiento.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // Equivalencia: tipos permitidos
    @Test
    void CP_E1_png_valido_se_acepta() {
        asientoDeAna();
        assertDoesNotThrow(() -> service.adjuntar(1, "voucher.png", PNG, "ana@finantel.pe"));
    }

    @Test
    void CP_E2_jpg_valido_se_acepta() {
        asientoDeAna();
        assertDoesNotThrow(() -> service.adjuntar(1, "voucher.jpg", JPG, "ana@finantel.pe"));
    }

    @Test
    void CP_E3_word_docx_no_se_acepta() {
        asientoDeAna();
        assertThrows(RuntimeException.class,
            () -> service.adjuntar(1, "informe.docx", ZIP_DOCX, "ana@finantel.pe"));
    }

    // Límites: tamaño y contenido vacío
    @Test
    void CP_L1_archivo_de_exactamente_5_mb_se_acepta() {
        asientoDeAna();
        byte[] contenido = new byte[5 * 1024 * 1024];
        System.arraycopy(PDF, 0, contenido, 0, PDF.length);

        assertDoesNotThrow(() -> service.adjuntar(1, "grande.pdf", contenido, "ana@finantel.pe"));
    }

    @Test
    void CS_A05_3_archivo_de_5_mb_mas_1_byte_se_rechaza() {
        asientoDeAna();
        byte[] contenido = new byte[5 * 1024 * 1024 + 1];
        System.arraycopy(PDF, 0, contenido, 0, PDF.length);

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.adjuntar(1, "grande.pdf", contenido, "ana@finantel.pe"));

        assertTrue(ex.getMessage().contains("5 MB"));
    }

    @Test
    void CP_L3_archivo_vacio_se_rechaza() {
        asientoDeAna();

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.adjuntar(1, "vacio.pdf", new byte[0], "ana@finantel.pe"));

        assertTrue(ex.getMessage().contains("vac"));
    }
}