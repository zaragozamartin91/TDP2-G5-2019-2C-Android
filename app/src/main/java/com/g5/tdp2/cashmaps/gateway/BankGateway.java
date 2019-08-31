package com.g5.tdp2.cashmaps.gateway;

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
}
