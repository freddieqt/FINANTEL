package com.finantel.pago_mercadopago.integracion;

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

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PagoMercadoPagoIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @MockitoBean
    private PagoService pagoService;

    @Test
    @DisplayName("IT-01: GET /web/cobros/{id}/enlace redirige a URL de Mercado Pago (HTTP 302)")
    void generar_enlace_endpoint_redirige_a_mercadopago() throws Exception {
        Integer asientoId = jdbc.queryForObject(
                "SELECT TOP 1 id FROM AsientosContables WHERE cuenta_contable = '1201' AND debe > 0",
                Integer.class
        );

        if (asientoId != null) {
            when(pagoService.crearEnlace(anyString(), any(BigDecimal.class), anyString()))
                    .thenReturn("https://www.mercadopago.com.pe/checkout/test123");

            mvc.perform(get("/web/cobros/" + asientoId + "/enlace")
                            .with(user("admin.prueba@finantel.pe").roles("ADMINISTRADOR")))
                    .andExpect(status().isFound())
                    .andExpect(header().string("Location", "https://www.mercadopago.com.pe/checkout/test123"));
        }
    }
}
