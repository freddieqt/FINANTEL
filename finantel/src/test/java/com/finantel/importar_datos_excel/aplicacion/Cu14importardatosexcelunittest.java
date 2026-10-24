package com.finantel.importar_datos_excel.aplicacion;

import com.finantel.importar_datos_excel.dominio.repository.ImportarDatosExcelRepository;
import com.finantel.registrar_accion.aplicacion.AuditoriaService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CU14ImportarDatosExcelUnitTest {

    @Mock
    private ImportarDatosExcelRepository repo;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private ImportarDatosExcelService service;

    @Test
    void CP_U1_archivo_vacio_se_rechaza() {
        MockMultipartFile vacio = new MockMultipartFile("archivo", "datos.csv", "text/csv", new byte[0]);

        Map<String, Object> r = service.importar(vacio, "prueba@finantel.pe");

        assertEquals(false, r.get("exito"));
        assertEquals("Error: archivo vacío", r.get("mensaje"));
    }

    @Test
    void CP_U2_formato_no_soportado_se_rechaza() {
        MockMultipartFile txt = new MockMultipartFile("archivo", "datos.txt", "text/plain",
            "contenido".getBytes(StandardCharsets.UTF_8));

        Map<String, Object> r = service.importar(txt, "prueba@finantel.pe");

        assertEquals(false, r.get("exito"));
        assertEquals("Error: formato inválido", r.get("mensaje"));
    }

    @Test
    void CP_U3_csv_valido_guarda_los_asientos_y_audita() {
        String csv = "tipo,descripcion,debe,haber\nCierre,Prueba A,100,0\nAjuste,Prueba B,0,50\n";
        MockMultipartFile archivo = new MockMultipartFile("archivo", "datos.csv", "text/csv",
            csv.getBytes(StandardCharsets.UTF_8));

        Map<String, Object> r = service.importar(archivo, "prueba@finantel.pe");

        assertEquals(true, r.get("exito"));
        assertEquals(2, r.get("total"));
        verify(repo).saveAll(anyList());
        verify(auditoriaService).registrar(any(), any(), any(), any(), any());
    }
}