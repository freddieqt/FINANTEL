package com.finantel.emitir_comprobantes.integracion;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmitirComprobantesIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void limpiar() {
        jdbcTemplate.update("DELETE FROM Comprobantes WHERE numero IN ('F001-IT01', 'F001-IT02')");
    }

    private static final String VALIDO =
        "{\"numero\":\"F001-IT01\",\"fecha\":\"2026-10-23\",\"tipo\":\"Factura\",\"cliente\":\"Empresa ABC SAC\",\"monto\":1500.0}";

    private static final String MONTO_CERO =
        "{\"numero\":\"F001-IT02\",\"tipo\":\"Factura\",\"cliente\":\"Empresa ABC SAC\",\"monto\":0.0}";

    @Test
    void CP_I1_listar_comprobantes_responde_un_arreglo() throws Exception {
        mvc.perform(get("/comprobantes")
                .with(user("admin@finantel.pe")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    void CP_I2_emitir_comprobante_valido_responde_ok() throws Exception {
        mvc.perform(post("/comprobantes")
                .with(user("admin@finantel.pe"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALIDO))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("emitido correctamente")));
    }

    @Test
    void CP_I3_emitir_con_monto_cero_responde_400() throws Exception {
        mvc.perform(post("/comprobantes")
                .with(user("admin@finantel.pe"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(MONTO_CERO))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("mayor a 0")));
    }

    @Test
    void CP_I4_abstract_factory_factura_responde_ok() throws Exception {
        mvc.perform(get("/comprobantes/abstract-factory/factura")
                .with(user("admin@finantel.pe")))
            .andExpect(status().isOk());
    }
}