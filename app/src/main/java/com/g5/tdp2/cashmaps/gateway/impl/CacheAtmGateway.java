package com.g5.tdp2.cashmaps.gateway.impl;

import com.g5.tdp2.cashmaps.domain.Atm;
import com.g5.tdp2.cashmaps.gateway.AtmGateway;
import com.g5.tdp2.cashmaps.gateway.GatewayException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Gateway de ATMs que cachea las solicitudes de atms anteriores
 */
public class CacheAtmGateway implements AtmGateway {
    private AtmGateway atmGateway;
    private Map<String, List<Atm>> atmMap = new HashMap<>();

    public CacheAtmGateway(AtmGateway atmGateway) {
        this.atmGateway = atmGateway;
    }

    @Override
    public List<Atm> getAtms(Request request) throws GatewayException {
        String key = request.toString();
        atmMap.putIfAbsent(
                key,
                Optional.ofNullable(atmMap.get(key)).orElseGet(() -> atmGateway.getAtms(request))
        );
        return atmMap.get(key);
    }
}
