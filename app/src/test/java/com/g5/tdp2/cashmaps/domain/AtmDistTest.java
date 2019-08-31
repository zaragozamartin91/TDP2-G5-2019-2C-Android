package com.g5.tdp2.cashmaps.domain;

import com.g5.tdp2.cashmaps.domain.AtmDist;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class AtmDistTest {
    @Test
    public void radiiShouldReturnAllAvailableRadiusValues() {
        List<Integer> radii = AtmDist.radii();
        assertTrue(radii.contains(AtmDist.R_100.radius));
        assertTrue(radii.contains(AtmDist.R_200.radius));
        assertTrue(radii.contains(AtmDist.R_500.radius));
        assertTrue(radii.contains(AtmDist.R_1000.radius));
    }

    @Test
    public void getDefaultReturns500Radius() {
        assertEquals(500, AtmDist.getDefault().radius);
    }

    /*
    * -58.37090179	-34.60581294
-58.37097578	-34.60509000

    * */
}