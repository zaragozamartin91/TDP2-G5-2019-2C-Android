package com.g5.tdp2.cashmaps.domain;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Radios de distancia permitidos para los filtros
 */
public enum AtmDist {
    R_100(100), R_200(200), R_500(500), R_1000(1000);

    /**
     * Distancia radio en metros
     */
    public final int radius;

    AtmDist(int radius) {
        this.radius = radius;
    }

    /**
     * Obtiene el radio de distancia por defecto
     *
     * @return radio de distancia por defecto
     */
    public static AtmDist getDefault() {
        return R_500;
    }

    /**
     * Obtiene todos los valores de radios disponibles. Usar junto a un ArrayAdapter para
     * cargar los filtros.
     *
     * @return todos los valores de radios disponibles
     */
    public static List<Integer> radii() {
        return Arrays.stream(AtmDist.values()).map(a -> a.radius).collect(Collectors.toList());
    }
}
