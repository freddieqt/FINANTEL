package com.finantel.adjuntar_documento.integracion;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
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
class AdjuntarDocumentoIT {

    static final byte[] PDF = {0x25, 0x50, 0x44, 0x46, 0x2D, 0x31, 0x2E, 0x34};
    static final byte[] EXE = {0x4D, 0x5A, (byte) 0x90, 0x00};
    static final String ADMIN = "admin.prueba@finantel.pe";

    static final String ASIENTO =
        "{\"tipo\":\"Ajuste\",\"descripcion\":\"ADJ IT\",\"debe\":5000.0,\"haber\":5000.0,"
        + "\"cuentaContable\":\"1011\",\"moneda\":\"PEN\",\"fechaContable\":\"2026-10-23\","
        + "\"periodoId\":1,\"tipoCambio\":1.0}";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    private int crearAsiento() throws Exception {
        mvc.perform(post("/asientos").with(user(ADMIN)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(ASIENTO))
            .andExpect(status().isOk());
        return jdbc.queryForObject(
            "SELECT TOP 1 id FROM AsientosContables WHERE descripcion = 'ADJ IT' ORDER BY id DESC", Integer.class);
    }

    private MockMultipartFile archivo(String nombre, String mime, byte[] contenido) {
        return new MockMultipartFile("archivo", nombre, mime, contenido);
    }

    @AfterEach
    void limpiar() {
        jdbc.update("DELETE FROM DocumentosAsiento WHERE usuario = ?", ADMIN);
        jdbc.update("DELETE FROM AsientosContables WHERE descripcion LIKE 'ADJ%'");
    }

    @Test
    void IT1_subir_pdf_valido_responde_ok_y_guarda_fila() throws Exception {
        int id = crearAsiento();

        mvc.perform(multipart("/asientos/{id}/documento", id)
                .file(archivo("factura.pdf", "application/pdf", PDF))
                .with(user(ADMIN)).with(csrf()))
            .andExpect(status().isOk());

        assertEquals(1, jdbc.queryForObject(
            "SELECT COUNT(*) FROM DocumentosAsiento WHERE nombreOriginal = 'factura.pdf'", Integer.class));
    }

    @Test
    void IT2_subir_exe_responde_400_y_no_guarda() throws Exception {
        int id = crearAsiento();

        mvc.perform(multipart("/asientos/{id}/documento", id)
                .file(archivo("virus.exe", "application/octet-stream", EXE))
                .with(user(ADMIN)).with(csrf()))
            .andExpect(status().isBadRequest());

        assertEquals(0, jdbc.queryForObject(
            "SELECT COUNT(*) FROM DocumentosAsiento WHERE nombreOriginal = 'virus.exe'", Integer.class));
    }

    @Test
    void IT3_el_dueno_descarga_el_documento_con_su_contenido() throws Exception {
        int id = crearAsiento();
        mvc.perform(multipart("/asientos/{id}/documento", id)
                .file(archivo("factura.pdf", "application/pdf", PDF))
                .with(user(ADMIN)).with(csrf()))
            .andExpect(status().isOk());
        int docId = jdbc.queryForObject(
            "SELECT TOP 1 id FROM DocumentosAsiento WHERE nombreOriginal = 'factura.pdf' ORDER BY id DESC", Integer.class);

        mvc.perform(get("/asientos/documento/{docId}", docId).with(user(ADMIN)))
            .andExpect(status().isOk())
            .andExpect(content().bytes(PDF));
    }
}