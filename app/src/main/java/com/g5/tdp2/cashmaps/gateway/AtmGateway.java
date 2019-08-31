package com.g5.tdp2.cashmaps.gateway;

import com.g5.tdp2.cashmaps.domain.Atm;
import com.g5.tdp2.cashmaps.domain.AtmNet;

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
    List<Atm> getAtms(Request request) throws GatewayException;

    class Request {
        public final AtmNet net;
        public final String bank;

        /**
         * Filtro por red y banco
         *
         * @param net  Red
         * @param bank Banco
         */
        public Request(AtmNet net, String bank) {
            this.net = net;
            this.bank = bank;
        }

        /**
         * Filtro solo por red
         *
         * @param net Red
         */
        public Request(AtmNet net) {
            this(net, null);
        }

        /**
         * Request sin filtro alguno
         */
        public Request() {
            this(null, null);
        }
    }
}
