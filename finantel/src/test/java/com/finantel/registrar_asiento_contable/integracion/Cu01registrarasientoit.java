package com.finantel.registrar_asiento_contable.integracion;

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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CU01RegistrarAsientoIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String VALIDO =
        "{\"tipo\":\"Ajuste\",\"descripcion\":\"CU01 IT\",\"debe\":5000.0,\"haber\":5000.0,"
        + "\"cuentaContable\":\"1011\",\"moneda\":\"PEN\",\"fechaContable\":\"2026-10-23\",\"periodoId\":1,\"tipoCambio\":1.0}";

    private static final String DEBE_CERO =
        "{\"tipo\":\"Ajuste\",\"descripcion\":\"CU01 IT\",\"debe\":0.0,\"haber\":5000.0,"
        + "\"cuentaContable\":\"1011\",\"moneda\":\"PEN\"}";

    @AfterEach
    void limpiar() {
        jdbcTemplate.update("DELETE FROM AsientosContables WHERE descripcion LIKE 'CU01%'");
    }

    @Test
    void CP_I1_registrar_asiento_valido_responde_ok() throws Exception {
        mvc.perform(post("/asientos")
                .with(user("admin.prueba@finantel.pe"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALIDO))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    void CP_I2_debe_en_cero_responde_400() throws Exception {
        mvc.perform(post("/asientos")
                .with(user("admin.prueba@finantel.pe"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(DEBE_CERO))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("debe")));
    }

    @Test
    void CP_I3_listar_asientos_responde_un_arreglo() throws Exception {
        mvc.perform(get("/asientos")
                .with(user("admin.prueba@finantel.pe")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }
}