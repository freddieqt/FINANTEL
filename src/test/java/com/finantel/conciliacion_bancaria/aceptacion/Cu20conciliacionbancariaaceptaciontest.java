package com.finantel.conciliacion_bancaria.aceptacion;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CU20ConciliacionBancariaAceptacionTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void HA01_el_extracto_se_concilia_con_uno_de_los_estados_validos() throws Exception {
        MockMultipartFile extracto = new MockMultipartFile(
            "extracto", "extracto.csv", "text/csv", "fecha,monto\n2026-10-01,100".getBytes());

        mvc.perform(multipart("/conciliacion/comparar")
                .file(extracto)
                .with(user("admin@finantel.pe"))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value(anyOf(is("Conciliado"), is("Con diferencias"))));
    }

    @Test
    void HA02_el_listado_de_movimientos_esta_disponible() throws Exception {
        mvc.perform(get("/conciliacion")
                .with(user("admin@finantel.pe")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }
}