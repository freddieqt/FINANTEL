package com.finantel.visualizar_reportes_graficos.integracion;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CU13VisualizarReportesGraficosIT {

    @Autowired
    private MockMvc mvc;

    @Test
    void CP_I1_resumen_responde_con_total_de_asientos() throws Exception {
        mvc.perform(get("/reportes-graficos/resumen")
                .with(user("admin@finantel.pe")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalAsientos").exists());
    }

    @Test
    void CP_I2_por_tipo_responde_un_objeto_con_conteos() throws Exception {
        mvc.perform(get("/reportes-graficos/por-tipo")
                .with(user("admin@finantel.pe")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isMap());
    }
}