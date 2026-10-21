package com.finantel.tipo_cambio.aplicacion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TipoCambioSbsServiceTest {

    private static final String API_URL = "https://tipo-cambio.test/api";
    private static final String INSERT_SQL =
        "INSERT INTO TipoCambio (fecha, compra, venta, fuente, estado) VALUES (GETDATE(), ?, ?, 'SBS', 'Activo')";

    @Test
    void cu22_respuestaNumericaActualizaFuenteYDevuelveTasas() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer api = MockRestServiceServer.createServer(restTemplate);
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        api.expect(requestTo(API_URL))
            .andRespond(withSuccess("{\"compra\":3.75,\"venta\":3.80}", MediaType.APPLICATION_JSON));
        TipoCambioSbsService service = new TipoCambioSbsService(restTemplate, jdbc, API_URL);

        var resultado = service.actualizar();

        assertEquals(3.75, resultado.get("compra"));
        assertEquals(3.8, resultado.get("venta"));
        verify(jdbc).update(eq(INSERT_SQL), eq(3.75), eq(3.8));
        api.verify();
    }

    @Test
    void cu22_tasasEnTextoSeConviertenAValoresNumericos() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer api = MockRestServiceServer.createServer(restTemplate);
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        api.expect(requestTo(API_URL))
            .andRespond(withSuccess("{\"compra\":\"3.75\",\"venta\":\"3.80\"}", MediaType.APPLICATION_JSON));
        TipoCambioSbsService service = new TipoCambioSbsService(restTemplate, jdbc, API_URL);

        assertEquals(3.8, service.actualizar().get("venta"));
        verify(jdbc).update(eq(INSERT_SQL), eq(3.75), eq(3.8));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "{}",
        "{\"compra\":0,\"venta\":3.80}",
        "{\"compra\":3.75,\"venta\":-1}",
        "{\"compra\":\"no-numérica\",\"venta\":3.80}",
        "{\"compra\":1e309,\"venta\":3.80}"
    })
    void cu22_datosAusentesInvalidosONoPositivosNoSeGuardan(String json) {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer api = MockRestServiceServer.createServer(restTemplate);
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        api.expect(requestTo(API_URL))
            .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));
        TipoCambioSbsService service = new TipoCambioSbsService(restTemplate, jdbc, API_URL);

        assertThrows(IllegalStateException.class, service::actualizar);
        verify(jdbc, never()).update(eq(INSERT_SQL), eq(3.75), eq(3.8));
        api.verify();
    }
}
