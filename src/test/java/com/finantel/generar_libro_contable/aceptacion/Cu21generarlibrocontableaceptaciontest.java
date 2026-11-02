package com.finantel.generar_libro_contable.aceptacion;

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
class CU21GenerarLibroContableAceptacionTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void HA01_el_contador_obtiene_el_libro_contable() throws Exception {
        mvc.perform(get("/libro-contable")
                .with(user("prueba@finantel.pe")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    void HA02_el_libro_responde_sin_error_del_servidor() throws Exception {
        mvc.perform(get("/libro-contable")
                .with(user("admin@finantel.pe")))
            .andExpect(status().is2xxSuccessful());
    }
}