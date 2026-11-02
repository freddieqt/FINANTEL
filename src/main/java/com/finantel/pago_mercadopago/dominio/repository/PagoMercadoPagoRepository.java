package com.finantel.pago_mercadopago.dominio.repository;

import com.finantel.pago_mercadopago.dominio.entity.PagoMercadoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PagoMercadoPagoRepository extends JpaRepository<PagoMercadoPago, Integer> {
    Optional<PagoMercadoPago> findFirstByAsientoIdOrderByIdDesc(Integer asientoId);
    boolean existsByPaymentId(String paymentId);
}