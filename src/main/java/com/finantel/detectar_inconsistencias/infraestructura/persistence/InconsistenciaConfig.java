package com.finantel.detectar_inconsistencias.infraestructura.persistence;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class InconsistenciaConfig {

    public boolean estaBalanceado(Double debe, Double haber) {
        return BigDecimal.valueOf(debe)
            .subtract(BigDecimal.valueOf(haber))
            .abs()
            .compareTo(new BigDecimal("0.01")) <= 0;
    }
}