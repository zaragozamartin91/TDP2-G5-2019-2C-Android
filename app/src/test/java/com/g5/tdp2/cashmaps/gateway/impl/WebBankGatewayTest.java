package com.g5.tdp2.cashmaps.gateway.impl;

import com.g5.tdp2.cashmaps.gateway.BankGateway;

import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class WebBankGatewayTest {
    BankGateway bankGateway = new WebBankGateway();

    @Ignore
    @Test
    public void getBanks() {
        List<String> banks = bankGateway.getBanks();
        System.out.println(banks);
        assertTrue(banks.size() > 0);
    }
}