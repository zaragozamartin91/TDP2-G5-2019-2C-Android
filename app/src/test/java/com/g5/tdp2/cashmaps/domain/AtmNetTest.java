package com.g5.tdp2.cashmaps.domain;

import com.g5.tdp2.cashmaps.domain.AtmNet;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class AtmNetTest {
    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowAnExceptionOnInvalidNetName() {
        AtmNet.fromString("PEPE");
    }

    @Test
    public void shouldCreateAnAtmNetFromValidNetName() {
        String n = AtmNet.LINK.toString();
        assertNotNull(AtmNet.fromString(n));
    }
}