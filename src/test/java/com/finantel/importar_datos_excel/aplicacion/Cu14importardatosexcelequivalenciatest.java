package com.finantel.importar_datos_excel.aplicacion;

import com.finantel.importar_datos_excel.dominio.repository.ImportarDatosExcelRepository;
import com.finantel.importar_datos_excel.infraestructura.persistence.ImportarDatosExcelConfig;
import com.finantel.registrar_accion.aplicacion.AuditoriaService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CU14ImportarDatosExcelEquivalenciaTest {

    private final ImportarDatosExcelConfig config = new ImportarDatosExcelConfig();

    @Mock
    private ImportarDatosExcelRepository repo;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private ImportarDatosExcelService service;

    @ParameterizedTest
    @CsvSource({
        "datos.xlsx, true",
        "datos.xls, true",
        "datos.txt, false",
        "datos.pdf, false"
    })
    void CP_E1_clases_de_formato_se_aceptan_o_rechazan(String nombre, boolean esperado) {
        assertEquals(esperado, config.esFormatoValido(nombre));
    }

    @Test
    void CP_E2_fila_sin_tipo_se_omite() {
        String csv = "tipo,descripcion,debe,haber\n,Sin tipo,100,0\nCierre,Con tipo,100,0\n";
        MockMultipartFile archivo = new MockMultipartFile("archivo", "datos.csv", "text/csv",
            csv.getBytes(StandardCharsets.UTF_8));

        Map<String, Object> r = service.importar(archivo, "prueba@finantel.pe");

        assertEquals(1, r.get("total"));
    }
}