package com.finantel.tipo_cambio.infraestructura;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class TipoCambioHttpConfig {

    @Bean
    public RestTemplate tipoCambioRestTemplate() {
        return new RestTemplate();
    }
}
