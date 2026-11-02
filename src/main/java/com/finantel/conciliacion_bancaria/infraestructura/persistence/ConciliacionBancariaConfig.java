package com.finantel.conciliacion_bancaria.infraestructura.persistence;

import org.springframework.stereotype.Component;

@Component
public class ConciliacionBancariaConfig {

    public boolean estaConciliado(Double diferencia) {
        if (diferencia == null) {
            return false; // Evita el NullPointerException si la diferencia es nula
        }
        return Math.abs(diferencia) < 0.01;
    }
}