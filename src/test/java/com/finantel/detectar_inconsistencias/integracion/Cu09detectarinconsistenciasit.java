package com.finantel.detectar_inconsistencias.integracion;

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
class CU09DetectarInconsistenciasIT {

    @Autowired
    private MockMvc mvc;

    @Test
    void CP_I1_detectar_todos_responde_un_arreglo() throws Exception {
        mvc.perform(get("/inconsistencias")
                .with(user("admin@finantel.pe")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    void CP_I2_detectar_todos_responde_en_formato_json() throws Exception {
        mvc.perform(get("/inconsistencias")
                .with(user("admin@finantel.pe")))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("application/json"));
    }
}