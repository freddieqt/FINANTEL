package com.finantel.registrar_asiento_contable.aceptacion;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CU01RegistrarAsientoAceptacionTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String VALIDO =
        "{\"tipo\":\"Ajuste\",\"descripcion\":\"CU01 HA01\",\"debe\":5000.0,\"haber\":5000.0,"
        + "\"cuentaContable\":\"1011\",\"moneda\":\"PEN\",\"fechaContable\":\"2026-10-23\",\"periodoId\":1,\"tipoCambio\":1.0}";

    private static final String DESCUADRADO =
        "{\"tipo\":\"Ajuste\",\"descripcion\":\"CU01 HA02\",\"debe\":5000.0,\"haber\":4000.0,"
        + "\"cuentaContable\":\"1011\",\"moneda\":\"PEN\"}";

    private int contar(String descripcion) {
        Integer total = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM AsientosContables WHERE descripcion = ?", Integer.class, descripcion);
        return total;
    }

    @AfterEach
    void limpiar() {
        jdbcTemplate.update("DELETE FROM AsientosContables WHERE descripcion LIKE 'CU01%'");
    }

    @Test
    void HA01_el_asiento_cuadrado_queda_registrado_en_la_base() throws Exception {
        mvc.perform(post("/asientos")
                .with(user("admin.prueba@finantel.pe"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALIDO))
            .andExpect(status().isOk());

        assertEquals(1, contar("CU01 HA01"));
    }

    @Test
    void HA02_un_asiento_descuadrado_no_se_registra() throws Exception {
        mvc.perform(post("/asientos")
                .with(user("admin.prueba@finantel.pe"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(DESCUADRADO))
            .andExpect(status().isBadRequest());

        assertEquals(0, contar("CU01 HA02"));
    }
}