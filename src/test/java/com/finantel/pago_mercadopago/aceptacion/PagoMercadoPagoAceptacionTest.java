package com.finantel.pago_mercadopago.aceptacion;

import com.finantel.pago_mercadopago.aplicacion.PagoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PagoMercadoPagoAceptacionTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @MockitoBean
    private PagoService pagoService;

    @Test
    @DisplayName("ACEP-01: Criterio de aceptación - Si el pago no está aprobado en MP, se informa al usuario")
    void verificar_pago_no_aprobado_informa_mensaje_adecuado() throws Exception {
        Integer asientoId = jdbc.queryForObject(
                "SELECT TOP 1 id FROM AsientosContables WHERE cuenta_contable = '1201' AND debe > 0",
                Integer.class
        );

        if (asientoId != null) {
            when(pagoService.buscarPagoAprobado(any())).thenReturn(Optional.empty());

            mvc.perform(get("/web/cobros/" + asientoId + "/verificar")
                            .with(user("admin.prueba@finantel.pe").roles("ADMINISTRADOR")))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Todav")));
        }
    }
}
