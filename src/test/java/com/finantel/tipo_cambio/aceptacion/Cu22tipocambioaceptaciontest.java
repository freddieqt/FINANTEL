package com.finantel.tipo_cambio.aceptacion;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClientException;
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
class CU22TipoCambioAceptacionTest {

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

    private int contarTasas() {
        Integer filas = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM TipoCambio WHERE fuente = 'SBS' AND compra = ?", Integer.class, 3.701);
        return filas;
    }

    @AfterEach
    void limpiar() {
        jdbcTemplate.update("DELETE FROM TipoCambio WHERE fuente = 'SBS' AND compra = ?", 3.701);
    }

    @Test
    void HA01_la_tasa_del_dia_queda_registrada_como_activa() {
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(respuesta(3.701, 3.751));

        service.actualizar();

        assertEquals(1, contarTasas());
    }

    @Test
    void HA02_si_la_api_no_responde_no_se_guarda_nada() {
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
            .thenThrow(new RestClientException("API caída"));

        assertThrows(RestClientException.class, () -> service.actualizar());
        assertEquals(0, contarTasas());
    }
}