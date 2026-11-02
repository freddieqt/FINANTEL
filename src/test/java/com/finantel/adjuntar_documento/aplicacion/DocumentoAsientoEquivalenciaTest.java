package com.finantel.adjuntar_documento.aplicacion;

import com.finantel.adjuntar_documento.aplicacion.DocumentoAsientoService;
import com.finantel.adjuntar_documento.aplicacion.StorageDocumentos;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class DocumentoAsientoEquivalenciaTest {

    @Mock DocumentoAsientoRepository repo;
    @Mock AsientoContableRepository asientoRepo;
    @Mock StorageDocumentos storage;
    @Mock AuditoriaService auditoria;
    @InjectMocks DocumentoAsientoService service;

    // ENTRADA: asiento id = 1 (de ana@finantel.pe); archivo = "voucher.jpeg" con cabecera JPEG (FF D8 FF E0)
    // RESULTADO: El documento se adjunta sin errores
    @Test
    void EQ_CU_adjuntar_voucher_jpeg_se_acepta() {
        AsientoContable a = new AsientoContable();
        a.setUsuario("ana@finantel.pe");
        lenient().when(asientoRepo.findById(1)).thenReturn(Optional.of(a));
        lenient().when(storage.guardar(any(byte[].class), anyString())).thenReturn("voucher.jpeg");
        lenient().when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
        byte[] jpeg = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0, 0, 0};
        assertDoesNotThrow(() -> service.adjuntar(1, "voucher.jpeg", jpeg, "ana@finantel.pe"));
    }
}
