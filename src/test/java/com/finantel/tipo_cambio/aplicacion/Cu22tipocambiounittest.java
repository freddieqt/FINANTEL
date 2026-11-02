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
class CU22TipoCambioUnitTest {

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
    void CP_U1_api_con_tasas_validas_devuelve_compra_y_venta() {
        when(restTemplate.getForObject(URL, Map.class)).thenReturn(respuesta(3.70, 3.75));

        Map<String, Double> r = service().actualizar();

        assertEquals(3.70, r.get("compra"), 0.0001);
        assertEquals(3.75, r.get("venta"), 0.0001);
    }

    @Test
    void CP_U2_api_sin_datos_lanza_error() {
        when(restTemplate.getForObject(URL, Map.class)).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> service().actualizar());
    }

    @Test
    void CP_U3_falta_la_venta_lanza_error_y_no_guarda() {
        when(restTemplate.getForObject(URL, Map.class)).thenReturn(respuesta(3.70, null));

        assertThrows(IllegalStateException.class, () -> service().actualizar());
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void CP_U4_actualizar_guarda_el_tipo_de_cambio_en_la_tabla() {
        when(restTemplate.getForObject(URL, Map.class)).thenReturn(respuesta(3.70, 3.75));

        service().actualizar();

        verify(jdbcTemplate).update(contains("INSERT INTO TipoCambio"), eq(3.70), eq(3.75));
    }
}