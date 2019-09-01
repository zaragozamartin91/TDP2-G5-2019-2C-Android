package com.g5.tdp2.cashmaps.gateway.impl;

import com.g5.tdp2.cashmaps.domain.Atm;
import com.g5.tdp2.cashmaps.domain.AtmNet;
import com.g5.tdp2.cashmaps.gateway.AtmGateway;
import com.g5.tdp2.cashmaps.gateway.AtmRequest;

import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class WebAtmGatewayTest {
    AtmGateway atmGateway = new WebAtmGateway("https://tdp2-atm-api.herokuapp.com");

    @Ignore
    @Test
    public void getAtms() {
        String bank = "BBVA Banco Francés";
        AtmNet net = AtmNet.fromString("BANELCO");
        List<Atm> atms = atmGateway.getAtms(new AtmRequest(net, bank));

        System.out.println(atms);

        assertTrue(atms.size() > 0);
        atms.forEach(a -> assertTrue(net.equals(a.getNet()) && bank.equals(a.getBank())));
    }
}