package com.finantel.buscar_asiento.integracion;

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
class CU08BuscarAsientoIT {

    @Autowired
    private MockMvc mvc;

    @Test
    void CP_I1_buscar_por_tipo_responde_un_arreglo() throws Exception {
        mvc.perform(get("/buscar-asiento/tipo/Cierre")
                .with(user("admin@finantel.pe")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    void CP_I2_buscar_por_estado_responde_un_arreglo() throws Exception {
        mvc.perform(get("/buscar-asiento/estado/Activo")
                .with(user("admin@finantel.pe")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }
}