package com.finantel.registrar_accion.aceptacion;

import com.finantel.registrar_accion.aplicacion.AuditoriaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CU04AuditoriaAceptacionTest {

    private static final String EMAIL = "prueba@finantel.pe";

    @Autowired private MockMvc mvc;
    @Autowired private AuditoriaService servicio;

    @Test
    void HA01_inicio_de_sesion_queda_trazado_con_usuario_y_fecha() throws Exception {
        mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + EMAIL + "\",\"password\":\"clave123\"}"))
            .andExpect(status().isOk());

        var inicio = servicio.filtrarPorUsuario(EMAIL).stream()
                .filter(a -> "Inicio de sesión".equals(a.getAccion()))
                .findFirst();
        assertTrue(inicio.isPresent());
        assertNotNull(inicio.get().getFecha());
    }

    @Test
    void HA02_intento_fallido_queda_trazado() throws Exception {
        mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + EMAIL + "\",\"password\":\"mal12345\"}"))
            .andExpect(status().isUnauthorized());

        boolean trazado = servicio.filtrarPorUsuario(EMAIL).stream()
                .anyMatch(a -> "Inicio de sesión fallido".equals(a.getAccion()));
        assertTrue(trazado);
    }
}