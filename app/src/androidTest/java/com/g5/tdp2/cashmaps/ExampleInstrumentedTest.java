package com.g5.tdp2.cashmaps;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.g5.tdp2.cashmaps.domain.AtmDist;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        assertEquals("com.g5.tdp2.cashmaps", appContext.getPackageName());
    }

    @Test
    public void testDistance() {
        double distanceMts = AtmDist.distanceMts(-34.60581294d, -58.37090179d, -34.60508393d, -58.37097578d);
        assertTrue(distanceMts > 0d);
    }
}
