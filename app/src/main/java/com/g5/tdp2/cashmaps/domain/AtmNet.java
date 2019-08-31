package com.g5.tdp2.cashmaps.domain;

import com.google.android.gms.common.util.Strings;

/**
 * Redes de cajero disponibles o existentes
 */
public enum AtmNet {
    LINK, BANELCO;

    /**
     * Obtiene una instancia de red a partir de un string.
     *
     * @param net Nombre
     * @return instancia de red.
     */
    public static AtmNet fromString(String net) {
        try {
            return AtmNet.valueOf(Strings.isEmptyOrWhitespace(net) ? "" : net.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Red de cajeros " + net + " es invalida!");
        }
    }
}
