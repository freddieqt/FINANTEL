package com.finantel.tipo_cambio.aplicacion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class TipoCambioSbsService {

    private final RestTemplate restTemplate;
    private final JdbcTemplate jdbcTemplate;
    private final String apiUrl;

    public TipoCambioSbsService(
        RestTemplate restTemplate,
        JdbcTemplate jdbcTemplate,
        @Value("${app.tipo-cambio.url:https://api.apis.net.pe/v1/tipo-cambio-sunat}") String apiUrl
    ) {
        this.restTemplate = restTemplate;
        this.jdbcTemplate = jdbcTemplate;
        this.apiUrl = apiUrl;
    }

    public Map<String, Double> actualizar() {
        Map<String, Object> response = restTemplate.getForObject(apiUrl, Map.class);
        if (response == null || response.get("compra") == null || response.get("venta") == null) {
            throw new IllegalStateException("La API no devolvió datos completos de tipo de cambio");
        }

        double compra = convertirTasa(response.get("compra"));
        double venta = convertirTasa(response.get("venta"));
        if (compra <= 0 || venta <= 0) {
            throw new IllegalStateException("Las tasas de compra y venta deben ser mayores a cero");
        }

        jdbcTemplate.update(
            "INSERT INTO TipoCambio (fecha, compra, venta, fuente, estado) VALUES (GETDATE(), ?, ?, 'SBS', 'Activo')",
            compra,
            venta
        );
        return Map.of("compra", compra, "venta", venta);
    }

    private double convertirTasa(Object value) {
        try {
            double tasa = value instanceof Number number
                ? number.doubleValue()
                : Double.parseDouble(value.toString());
            if (!Double.isFinite(tasa)) {
                throw new IllegalStateException("La API devolvió una tasa no finita");
            }
            return tasa;
        } catch (NumberFormatException e) {
            throw new IllegalStateException("La API devolvió una tasa no numérica", e);
        }
    }
}
