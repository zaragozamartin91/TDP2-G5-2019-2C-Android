package com.g5.tdp2.cashmaps.gateway.impl;

import com.g5.tdp2.cashmaps.domain.AtmNet;
import com.g5.tdp2.cashmaps.gateway.BankGateway;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static com.g5.tdp2.cashmaps.domain.AtmNet.BANELCO;
import static com.g5.tdp2.cashmaps.domain.AtmNet.LINK;

/**
 * Gateway de bancos que cachea respuestas
 */
public class CacheBankGateway implements BankGateway {
    private final AtomicReference<List<String>> allBanks = new AtomicReference<>();
    private final AtomicReference<List<String>> banelcoBanks = new AtomicReference<>();
    private final AtomicReference<List<String>> linkBanks = new AtomicReference<>();

    private final BankGateway bankGateway;

    public CacheBankGateway(BankGateway bankGateway) {
        this.bankGateway = bankGateway;
    }

    @Override
    public List<String> getBanks() {
        synchronized (allBanks) {
            List<String> banks = Optional.ofNullable(allBanks.get()).orElseGet(bankGateway::getBanks);
            allBanks.compareAndSet(null, banks);
        }
        return allBanks.get();
    }

    @Override
    public List<String> getBanks(AtmNet net) {
        switch (net) {
            case LINK:
                synchronized (linkBanks) {
                    List<String> banks = Optional.ofNullable(linkBanks.get()).orElseGet(() -> bankGateway.getBanks(LINK));
                    linkBanks.compareAndSet(null, banks);
                }
                return linkBanks.get();
            case BANELCO:
                synchronized (banelcoBanks) {
                    List<String> banks = Optional.ofNullable(banelcoBanks.get()).orElseGet(() -> bankGateway.getBanks(BANELCO));
                    banelcoBanks.compareAndSet(null, banks);
                }
                return banelcoBanks.get();
            default:
                throw new IllegalArgumentException("Red " + net + " invalida!");
        }
    }
}
