package com.finantel.generar_estados_financieros.integracion;

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
class CU11GenerarEstadosIT {

    @Autowired
    private MockMvc mvc;

    @Test
    void CP_I1_balance_general_responde_con_utilidad_neta() throws Exception {
        mvc.perform(get("/estados-financieros/balance")
                .with(user("admin@finantel.pe")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.utilidadNeta").exists());
    }

    @Test
    void CP_I2_balance_web_responde_con_activos_en_arreglo() throws Exception {
        mvc.perform(get("/estados-financieros/web/balance")
                .with(user("admin@finantel.pe")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.activos").isArray());
    }
}