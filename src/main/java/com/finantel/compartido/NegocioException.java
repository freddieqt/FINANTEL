package com.finantel.compartido;

public class NegocioException extends RuntimeException {
    public NegocioException(String mensaje) {
        super(mensaje);
    }
}