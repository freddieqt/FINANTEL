package com.finantel.validar_cuentas.integracion;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CU10ValidarCuentasIT {

    @Autowired
    private MockMvc mvc;

    @Test
    void CP_I1_listar_cuentas_activas_responde_un_arreglo() throws Exception {
        mvc.perform(get("/validar-cuentas")
                .with(user("admin@finantel.pe")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    void CP_I2_crear_cuenta_invalida_responde_400_con_el_error() throws Exception {
        String cuerpo = """
            {"codigo":"ABC","descripcion":"Prueba","clasificacion":"Pasivo",
             "tipo":"X","estado":"Y"}
            """;

        mvc.perform(post("/validar-cuentas")
                .with(user("admin@finantel.pe"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").exists());
    }
}