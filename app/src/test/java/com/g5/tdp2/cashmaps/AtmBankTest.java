package com.g5.tdp2.cashmaps;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class AtmBankTest {

    @Test
    public void normalizeShouldSortBankNamesInAscendingOrderAndNormalizeThem() {
        List<String> banks = Arrays.asList("Macro", "Galicia", "Frances", "BBUVA");
        System.out.println(banks);
        assertArrayEquals(new String[]{"BBUVA", "FRANCES", "GALICIA", "MACRO"}, AtmBank.INSTANCE.normalize(banks).toArray());
    }
}