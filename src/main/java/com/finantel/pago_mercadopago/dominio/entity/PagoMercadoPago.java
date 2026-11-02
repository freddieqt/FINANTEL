package com.finantel.pago_mercadopago.dominio.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PagosMercadoPago")
public class PagoMercadoPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer asientoId;
    private String numeroCorrelativo;
    private BigDecimal monto;
    private String preferenceId;
    private String initPoint;
    private String paymentId;
    private String estado;
    private LocalDateTime fechaCreacion;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getAsientoId() {
		return asientoId;
	}
	public void setAsientoId(Integer asientoId) {
		this.asientoId = asientoId;
	}
	public String getNumeroCorrelativo() {
		return numeroCorrelativo;
	}
	public void setNumeroCorrelativo(String numeroCorrelativo) {
		this.numeroCorrelativo = numeroCorrelativo;
	}
	public BigDecimal getMonto() {
		return monto;
	}
	public void setMonto(BigDecimal monto) {
		this.monto = monto;
	}
	public String getPreferenceId() {
		return preferenceId;
	}
	public void setPreferenceId(String preferenceId) {
		this.preferenceId = preferenceId;
	}
	public String getInitPoint() {
		return initPoint;
	}
	public void setInitPoint(String initPoint) {
		this.initPoint = initPoint;
	}
	public String getPaymentId() {
		return paymentId;
	}
	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}
	public String getEstado() {
		return estado;
	}
	public void setEstado(String estado) {
		this.estado = estado;
	}
	public LocalDateTime getFechaCreacion() {
		return fechaCreacion;
	}
	public void setFechaCreacion(LocalDateTime fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

    // Genera getters y setters desde el IDE: clic derecho → Source → Generate Getters and Setters
}