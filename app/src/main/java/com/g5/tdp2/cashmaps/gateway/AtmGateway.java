package com.g5.tdp2.cashmaps.gateway;

import com.g5.tdp2.cashmaps.domain.Atm;

import java.util.List;

/**
 * Permite obtener ATMs
 */
public interface AtmGateway {
    /**
     * Obtiene ATMs aplicando un filtro
     *
     * @param request Filtro de request
     * @return Bancos filtrados por el request indicado
     * @throws GatewayException Si ocurre un error al obtener los ATMs
     */
    List<Atm> getAtms(AtmRequest request) throws GatewayException;

}
