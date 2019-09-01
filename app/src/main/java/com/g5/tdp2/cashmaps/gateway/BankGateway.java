package com.g5.tdp2.cashmaps.gateway;

import com.g5.tdp2.cashmaps.domain.AtmNet;

import java.util.List;

/**
 * Obtiene el catalogo de bancos disponibles
 */
public interface BankGateway {
    /**
     * Obtiene el catalogo de bancos disponibles
     *
     * @return catalogo de bancos disponibles
     */
    List<String> getBanks();

    /**
     * Obtiene el catalogo de bancos disponibles de una red en particular
     *
     * @param net Red de cajeros
     * @return catalogo de bancos disponibles de una red en particular
     */
    List<String> getBanks(AtmNet net);
}
