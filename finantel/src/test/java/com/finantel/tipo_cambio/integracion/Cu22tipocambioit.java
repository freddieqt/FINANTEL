package com.finantel.tipo_cambio.integracion;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;

import com.finantel.tipo_cambio.aplicacion.TipoCambioSbsService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class CU22TipoCambioIT {

    @MockitoBean
    private RestTemplate restTemplate;

    @Autowired
    private TipoCambioSbsService service;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Map<String, Object> respuesta(Object compra, Object venta) {
        Map<String, Object> m = new HashMap<>();
        m.put("compra", compra);
        m.put("venta", venta);
        return m;
    }

    @AfterEach
    void limpiar() {
        jdbcTemplate.update("DELETE FROM TipoCambio WHERE fuente = 'SBS' AND compra = ?", 3.701);
    }

    @Test
    void CP_I1_actualizar_con_api_valida_devuelve_y_guarda_la_tasa() {
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(respuesta(3.701, 3.751));

        Map<String, Double> r = service.actualizar();

        assertEquals(3.701, r.get("compra"), 0.0001);
        Integer filas = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM TipoCambio WHERE fuente = 'SBS' AND compra = ?", Integer.class, 3.701);
        assertEquals(1, filas);
    }

    @Test
    void CP_I2_api_con_datos_incompletos_lanza_error() {
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(respuesta(3.701, null));

        assertThrows(IllegalStateException.class, () -> service.actualizar());
    }
}