package com.finantel.tipo_cambio.aplicacion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CU22TipoCambioEquivalenciaTest {

    private static final String URL = "http://test/tc";

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private JdbcTemplate jdbcTemplate;

    private TipoCambioSbsService service() {
        return new TipoCambioSbsService(restTemplate, jdbcTemplate, URL);
    }

    private Map<String, Object> respuesta(Object compra, Object venta) {
        Map<String, Object> m = new HashMap<>();
        m.put("compra", compra);
        m.put("venta", venta);
        return m;
    }

    @Test
    void CP_E1_tasas_en_texto_numerico_se_aceptan() {
        when(restTemplate.getForObject(URL, Map.class)).thenReturn(respuesta("3.70", "3.75"));

        Map<String, Double> r = service().actualizar();

        assertEquals(3.70, r.get("compra"), 0.0001);
    }

    @Test
    void CP_E2_tasa_no_numerica_lanza_error() {
        when(restTemplate.getForObject(URL, Map.class)).thenReturn(respuesta("abc", "3.75"));

        assertThrows(IllegalStateException.class, () -> service().actualizar());
        verifyNoInteractions(jdbcTemplate);
    }
}