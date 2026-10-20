package com.finantel.pago_mercadopago.aplicacion;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.net.MPSearchRequest;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PagoService {

    public PagoService(@Value("${mercadopago.access-token}") String accessToken) {
        MercadoPagoConfig.setAccessToken(accessToken);
    }

    public String crearEnlace(String descripcion, BigDecimal monto, String referencia) throws Exception {
        PreferenceItemRequest item = PreferenceItemRequest.builder()
                .title(descripcion)
                .quantity(1)
                .unitPrice(monto)
                .currencyId("PEN")
                .build();

        PreferenceRequest request = PreferenceRequest.builder()
                .items(List.of(item))
                .externalReference(referencia)
                .build();

        Preference preference = new PreferenceClient().create(request);
        return preference.getInitPoint();
    }

    public Optional<String> buscarPagoAprobado(String referencia) throws Exception {
        PaymentClient client = new PaymentClient();

        MPSearchRequest request = MPSearchRequest.builder()
                .limit(50)
                .offset(0)
                .filters(Map.of("external_reference", referencia))
                .build();

        for (Payment p : client.search(request).getResults()) {
            if ("approved".equals(p.getStatus())) {
                return Optional.of(String.valueOf(p.getId()));
            }
        }
        return Optional.empty();
    }
}