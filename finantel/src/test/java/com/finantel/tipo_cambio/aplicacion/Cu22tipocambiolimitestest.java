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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CU22TipoCambioLimitesTest {

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
    void CP_L1_compra_igual_a_cero_lanza_error() {
        when(restTemplate.getForObject(URL, Map.class)).thenReturn(respuesta(0.0, 3.75));

        assertThrows(IllegalStateException.class, () -> service().actualizar());
    }

    @Test
    void CP_L2_venta_negativa_lanza_error() {
        when(restTemplate.getForObject(URL, Map.class)).thenReturn(respuesta(3.70, -1.0));

        assertThrows(IllegalStateException.class, () -> service().actualizar());
    }

    @Test
    void CP_L3_tasa_infinita_lanza_error() {
        when(restTemplate.getForObject(URL, Map.class)).thenReturn(respuesta("Infinity", 3.75));

        assertThrows(IllegalStateException.class, () -> service().actualizar());
    }

    @Test
    void CP_L4_tasa_minima_positiva_se_acepta() {
        when(restTemplate.getForObject(URL, Map.class)).thenReturn(respuesta(0.0001, 0.0002));

        Map<String, Double> r = service().actualizar();

        assertEquals(0.0001, r.get("compra"), 0.000001);
    }

    @Test
    void CP_L5_compra_ausente_lanza_error() {
        Map<String, Object> m = new HashMap<>();
        m.put("venta", 3.75);
        when(restTemplate.getForObject(URL, Map.class)).thenReturn(m);

        assertThrows(IllegalStateException.class, () -> service().actualizar());
    }
}