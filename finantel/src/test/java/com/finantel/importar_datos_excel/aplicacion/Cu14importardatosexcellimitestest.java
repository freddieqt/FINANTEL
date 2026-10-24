package com.finantel.importar_datos_excel.aplicacion;

import com.finantel.importar_datos_excel.dominio.repository.ImportarDatosExcelRepository;
import com.finantel.importar_datos_excel.infraestructura.persistence.ImportarDatosExcelConfig;
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

@ExtendWith(MockitoExtension.class)
class CU14ImportarDatosExcelLimitesTest {

    private final ImportarDatosExcelConfig config = new ImportarDatosExcelConfig();

    @Mock
    private ImportarDatosExcelRepository repo;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private ImportarDatosExcelService service;

    private MockMultipartFile csv(String contenido) {
        return new MockMultipartFile("archivo", "datos.csv", "text/csv",
            contenido.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void CP_L1_csv_solo_con_encabezado_importa_cero_registros() {
        Map<String, Object> r = service.importar(csv("tipo,descripcion,debe,haber\n"), "prueba@finantel.pe");

        assertEquals(true, r.get("exito"));
        assertEquals(0, r.get("total"));
    }

    @Test
    void CP_L2_fila_con_menos_de_cuatro_campos_se_omite() {
        Map<String, Object> r = service.importar(
            csv("tipo,descripcion,debe,haber\nCierre,Incompleta,100\n"), "prueba@finantel.pe");

        assertEquals(0, r.get("total"));
    }

    @Test
    void CP_L3_debe_no_numerico_se_guarda_como_cero() {
        Map<String, Object> r = service.importar(
            csv("tipo,descripcion,debe,haber\nCierre,Texto,abc,0\n"), "prueba@finantel.pe");

        assertEquals(1, r.get("total"));
        assertEquals(0.0, ((java.util.List<?>) r.get("asientos")).isEmpty() ? -1 :
            ((Number) ((Map<?, ?>) ((java.util.List<?>) r.get("asientos")).get(0)).get("debe")).doubleValue());
    }

    @Test
    void CP_L4_config_acepta_csv_porque_el_servicio_lo_procesa() {
        // Comportamiento esperado: la configuración debe reconocer .csv igual que el servicio.
        // Si falla, es una inconsistencia entre configuración y servicio: agregar .csv a esFormatoValido.
        assertTrue(config.esFormatoValido("datos.csv"));
    }

    @Test
    void CP_L5_nombre_nulo_no_es_formato_valido() {
        assertFalse(config.esFormatoValido(null));
    }
}