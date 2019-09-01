package com.g5.tdp2.cashmaps.gateway;

import com.g5.tdp2.cashmaps.domain.AtmNet;

public class AtmRequest {
    public final AtmNet net;
    public final String bank;

    /**
     * Filtro por red y banco
     *
     * @param net  Red
     * @param bank Banco
     */
    public AtmRequest(AtmNet net, String bank) {
        this.net = net;
        this.bank = bank;
    }

    /**
     * Filtro solo por red
     *
     * @param net Red
     */
    public AtmRequest(AtmNet net) {
        this(net, null);
    }

    /**
     * AtmRequest sin filtro alguno
     */
    public AtmRequest() {
        this(null, null);
    }

    @Override
    public String toString() {
        return "" + net + "-" + bank;
    }
}
