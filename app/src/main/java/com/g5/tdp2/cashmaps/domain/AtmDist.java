package com.g5.tdp2.cashmaps.domain;

import android.location.Location;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utilitario de distancias
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

    /**
     * Calcula la distancia en metros entre my ubicacion y un destino
     *
     * @param myLat  Mi latitud
     * @param myLon  Mi longitud
     * @param tgtLat Latitud destino
     * @param tgtLon Longitud destino
     * @return Distancia en metros calculada
     */
    public static double distanceMts(double myLat, double myLon, double tgtLat, double tgtLon) {
        Location myLocation = getLocation(myLat, myLon);
        Location targetLocation = getLocation(tgtLat, tgtLon);

        return targetLocation.distanceTo(myLocation);
    }

    private static Location getLocation(double myLat, double myLon) {
        Location myLocation = new Location("");
        myLocation.setLatitude(myLat);
        myLocation.setLongitude(myLon);
        return myLocation;
    }
}
