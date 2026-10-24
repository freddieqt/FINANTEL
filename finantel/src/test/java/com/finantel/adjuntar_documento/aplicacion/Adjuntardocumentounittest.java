package com.finantel.adjuntar_documento.aplicacion;

import com.finantel.adjuntar_documento.dominio.entity.DocumentoAsiento;
import com.finantel.adjuntar_documento.dominio.repository.DocumentoAsientoRepository;
import com.finantel.registrar_accion.aplicacion.AuditoriaService;
import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import com.finantel.registrar_asiento_contable.dominio.repository.AsientoContableRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdjuntarDocumentoUnitTest {

    // Firmas reales de archivo (magic bytes)
    static final byte[] PDF = {0x25, 0x50, 0x44, 0x46, 0x2D, 0x31, 0x2E, 0x34};
    static final byte[] PNG = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    static final byte[] EXE = {0x4D, 0x5A, (byte) 0x90, 0x00};

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
        lenient().when(storage.guardar(any(), anyString())).thenReturn("ruta/uuid.pdf");
        lenient().when(repo.save(any(DocumentoAsiento.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void CP_U1_pdf_valido_se_guarda_en_almacenamiento() {
        asientoDeAna();

        service.adjuntar(1, "factura.pdf", PDF, "ana@finantel.pe");

        verify(storage).guardar(eq(PDF), eq("pdf"));
        verify(repo).save(any(DocumentoAsiento.class));
    }

    @Test
    void CP_U2_adjuntar_registra_la_accion_en_auditoria() {
        asientoDeAna();

        service.adjuntar(1, "factura.pdf", PDF, "ana@finantel.pe");

        verify(auditoria).registrar(eq("ana@finantel.pe"), anyString(), eq("Documentos"), isNull(), contains("factura.pdf"));
    }

    @Test
    void CP_U3_el_dueno_puede_descargar_su_documento() {
        DocumentoAsiento doc = new DocumentoAsiento();
        doc.setUsuario("ana@finantel.pe");
        doc.setRuta("ruta/uuid.pdf");
        when(repo.findById(5)).thenReturn(Optional.of(doc));
        when(storage.leer("ruta/uuid.pdf")).thenReturn(PDF);

        assertArrayEquals(PDF, service.descargar(5, "ana@finantel.pe", "Auditor"));
    }

    @Test
    void CS_A01_2_otro_usuario_sin_rol_contador_no_puede_descargar() {
        DocumentoAsiento doc = new DocumentoAsiento();
        doc.setUsuario("ana@finantel.pe");
        doc.setRuta("ruta/uuid.pdf");
        when(repo.findById(5)).thenReturn(Optional.of(doc));

        assertThrows(AccessDeniedException.class,
            () -> service.descargar(5, "otro@finantel.pe", "Auditor"));
        verifyNoInteractions(storage);
    }

    @Test
    void CS_A05_1_ejecutable_con_extension_exe_se_rechaza() {
        asientoDeAna();

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.adjuntar(1, "virus.exe", EXE, "ana@finantel.pe"));

        assertTrue(ex.getMessage().contains("Tipo de archivo no permitido"));
        verify(storage, never()).guardar(any(), anyString());
    }

    @Test
    void CS_A05_2_extension_pdf_con_contenido_de_ejecutable_se_rechaza() {
        asientoDeAna();

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.adjuntar(1, "falso.pdf", EXE, "ana@finantel.pe"));

        assertTrue(ex.getMessage().contains("Tipo de archivo no permitido"));
        verify(storage, never()).guardar(any(), anyString());
    }

    @Test
    void CS_A01_1_nombre_con_ruta_relativa_no_altera_la_ruta_de_guardado() {
        asientoDeAna();

        service.adjuntar(1, "../../secreto.pdf", PDF, "ana@finantel.pe");

        ArgumentCaptor<DocumentoAsiento> captor = ArgumentCaptor.forClass(DocumentoAsiento.class);
        verify(repo).save(captor.capture());
        assertEquals("secreto.pdf", captor.getValue().getNombreOriginal());
        verify(storage).guardar(eq(PDF), eq("pdf"));
    }

    @Test
    void CP_U5_asiento_inexistente_no_recibe_documento() {
        when(asientoRepo.findById(99)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.adjuntar(99, "factura.pdf", PDF, "ana@finantel.pe"));

        assertTrue(ex.getMessage().contains("Asiento no encontrado"));
        verify(storage, never()).guardar(any(), anyString());
    }
}