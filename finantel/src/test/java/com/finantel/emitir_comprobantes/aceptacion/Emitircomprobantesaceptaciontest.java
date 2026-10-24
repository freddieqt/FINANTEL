package com.finantel.emitir_comprobantes.aceptacion;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmitirComprobantesAceptacionTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void limpiar() {
        jdbcTemplate.update("DELETE FROM Comprobantes WHERE numero IN ('F001-HA01', 'F001-HA02')");
    }

    private static final String VALIDO =
        "{\"numero\":\"F001-HA01\",\"fecha\":\"2026-10-23\",\"tipo\":\"Boleta\",\"cliente\":\"Cliente HA\",\"monto\":250.0}";

    private static final String SIN_CLIENTE =
        "{\"numero\":\"F001-HA02\",\"tipo\":\"Factura\",\"cliente\":\"\",\"monto\":250.0}";

    @Test
    void HA01_el_contador_emite_una_boleta_valida() throws Exception {
        mvc.perform(post("/comprobantes")
                .with(user("prueba@finantel.pe"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALIDO))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("emitido correctamente")));
    }

    @Test
    void HA02_no_se_emite_un_comprobante_sin_cliente() throws Exception {
        mvc.perform(post("/comprobantes")
                .with(user("prueba@finantel.pe"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(SIN_CLIENTE))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("obligatorio")));
    }
}