package com.finantel.adjuntar_documento.aceptacion;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdjuntarDocumentoAceptacionTest {

    static final byte[] PDF = {0x25, 0x50, 0x44, 0x46, 0x2D, 0x31, 0x2E, 0x34};
    static final String ADMIN = "admin.prueba@finantel.pe";
    static final String OTRO = "otro@finantel.pe";

    static final String ASIENTO =
        "{\"tipo\":\"Ajuste\",\"descripcion\":\"ADJ ACEPT\",\"debe\":5000.0,\"haber\":5000.0,"
        + "\"cuentaContable\":\"1011\",\"moneda\":\"PEN\",\"fechaContable\":\"2026-10-23\","
        + "\"periodoId\":1,\"tipoCambio\":1.0}";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    private int crearAsientoYSubirPdf() throws Exception {
        mvc.perform(post("/asientos").with(user(ADMIN)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(ASIENTO))
            .andExpect(status().isOk());
        int id = jdbc.queryForObject(
            "SELECT TOP 1 id FROM AsientosContables WHERE descripcion = 'ADJ ACEPT' ORDER BY id DESC", Integer.class);

        mvc.perform(multipart("/asientos/{id}/documento", id)
                .file(new MockMultipartFile("archivo", "respaldo.pdf", "application/pdf", PDF))
                .with(user(ADMIN)).with(csrf()))
            .andExpect(status().isOk());

        return jdbc.queryForObject(
            "SELECT TOP 1 id FROM DocumentosAsiento WHERE nombreOriginal = 'respaldo.pdf' ORDER BY id DESC", Integer.class);
    }

    @AfterEach
    void limpiar() {
        jdbc.update("DELETE FROM DocumentosAsiento WHERE usuario = ?", ADMIN);
        jdbc.update("DELETE FROM AsientosContables WHERE descripcion LIKE 'ADJ%'");
    }

    @Test
    void HA01_el_usuario_descarga_el_documento_que_adjunto() throws Exception {
        int docId = crearAsientoYSubirPdf();

        mvc.perform(get("/asientos/documento/{docId}", docId).with(user(ADMIN)))
            .andExpect(status().isOk())
            .andExpect(content().bytes(PDF));
    }

    @Test
    void HA02_otro_usuario_sin_permiso_no_descarga_el_documento() throws Exception {
        int docId = crearAsientoYSubirPdf();

        mvc.perform(get("/asientos/documento/{docId}", docId).with(user(OTRO)))
            .andExpect(status().isForbidden());
    }
}