package com.finantel.exportar_reportes.integracion;

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
class CU12ExportarReportesIT {

    @Autowired
    private MockMvc mvc;

    @Test
    void CP_I1_exportar_pdf_responde_con_el_reporte() throws Exception {
        mvc.perform(get("/exportar-reportes/pdf")
                .with(user("admin@finantel.pe")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Reporte PDF generado con")));
    }

    @Test
    void CP_I2_formato_no_soportado_responde_400() throws Exception {
        mvc.perform(get("/exportar-reportes/factory/xyz")
                .with(user("admin@finantel.pe")))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Tipo no soportado")));
    }
}