package com.finantel.importar_datos_excel.integracion;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CU14ImportarDatosExcelIT {

    @Autowired
    private MockMvc mvc;

    @Test
    void CP_I1_formato_no_soportado_responde_exito_falso() throws Exception {
        MockMultipartFile txt = new MockMultipartFile("archivo", "datos.txt", "text/plain",
            "contenido".getBytes(StandardCharsets.UTF_8));

        mvc.perform(multipart("/web/importar-excel")
                .file(txt)
                .with(user("admin@finantel.pe"))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Error: formato inválido")));
    }

    @Test
    void CP_I2_peticion_sin_archivo_responde_400() throws Exception {
        mvc.perform(multipart("/web/importar-excel")
                .with(user("admin@finantel.pe"))
                .with(csrf()))
            .andExpect(status().isBadRequest());
    }
}