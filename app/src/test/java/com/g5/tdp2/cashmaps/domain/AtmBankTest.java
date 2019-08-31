package com.g5.tdp2.cashmaps.domain;

import com.g5.tdp2.cashmaps.domain.AtmBank;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class AtmBankTest {

    @Test
    public void normalizeShouldSortBankNamesInAscendingOrder() {
        List<String> banks = Arrays.asList("Macro", "Galicia", "Frances", "BBUVA");
        System.out.println(banks);
        assertArrayEquals(new String[]{"BBUVA", "Frances", "Galicia", "Macro"}, AtmBank.INSTANCE.sort(banks).toArray());
    }
}