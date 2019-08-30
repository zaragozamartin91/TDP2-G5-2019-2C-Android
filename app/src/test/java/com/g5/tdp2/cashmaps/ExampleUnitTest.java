package com.g5.tdp2.cashmaps;

import org.json.JSONException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testMapBigDecimal() throws JSONException {
        /* Verifico que la latitud y longitud puedan representarse como doubles */
        double d = -34.605812942035;
        assertEquals("-34.605812942035", Double.valueOf(d).toString());

        double dd = -34.5941286372598;
        assertEquals("-34.5941286372598", Double.valueOf(dd).toString());

        assertEquals("BANELCO", AtmNet.BANELCO.toString());
        assertEquals("LINK", AtmNet.LINK.toString());
    }
}