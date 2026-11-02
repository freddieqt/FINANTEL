package com.finantel.registrar_asiento_contable.dominio.entity;

public class Partida {

    private String cuentaContable;
    private Double debe;
    private Double haber;

    public Partida() {}

    public Partida(String cuentaContable, Double debe, Double haber) {
        this.cuentaContable = cuentaContable;
        this.debe = debe;
        this.haber = haber;
    }

    public String getCuentaContable() { return cuentaContable; }
    public void setCuentaContable(String cuentaContable) { this.cuentaContable = cuentaContable; }

    public Double getDebe() { return debe; }
    public void setDebe(Double debe) { this.debe = debe; }

    public Double getHaber() { return haber; }
    public void setHaber(Double haber) { this.haber = haber; }
}