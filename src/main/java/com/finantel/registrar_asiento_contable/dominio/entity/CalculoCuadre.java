package com.finantel.registrar_asiento_contable.dominio.entity;

import java.util.List;

public final class CalculoCuadre {

    public static final double TOLERANCIA_CUADRE = 0.01;

    private CalculoCuadre() {}

    public static double totalDebe(List<Partida> partidas) {
        if (partidas == null) return 0.0;
        return partidas.stream()
            .mapToDouble(p -> p.getDebe() == null ? 0.0 : p.getDebe())
            .sum();
    }

    public static double totalHaber(List<Partida> partidas) {
        if (partidas == null) return 0.0;
        return partidas.stream()
            .mapToDouble(p -> p.getHaber() == null ? 0.0 : p.getHaber())
            .sum();
    }

    public static double diferencia(List<Partida> partidas) {
        return totalDebe(partidas) - totalHaber(partidas);
    }

    public static boolean estaCuadrado(List<Partida> partidas) {
        if (partidas == null || partidas.isEmpty()) return false;
        return Math.abs(diferencia(partidas)) <= TOLERANCIA_CUADRE;
    }

    public static String getEstadoCuadre(List<Partida> partidas) {
        return estaCuadrado(partidas) ? "Cuadrado" : "Descuadrado";
    }
}