package com.finantel.exportar_reportes.aceptacion;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CU12ExportarReportesAceptacionTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void HA01_exportar_excel_entrega_el_reporte_con_el_conteo_de_asientos() throws Exception {
        mvc.perform(get("/exportar-reportes/excel")
                .with(user("prueba@finantel.pe")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Reporte Excel generado con")))
            .andExpect(content().string(containsString("asientos contables")));
    }

    @Test
    void HA02_la_fabrica_PDF_identifica_el_patron_y_el_formato() throws Exception {
        mvc.perform(get("/exportar-reportes/factory/pdf")
                .with(user("prueba@finantel.pe")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Patrón Factory Method")))
            .andExpect(content().string(containsString("Tipo: PDF")));
    }
}