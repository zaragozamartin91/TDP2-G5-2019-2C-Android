package com.g5.tdp2.cashmaps;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

    @Test
    public void handleResponse() throws IOException {
        String response = "[{\"id\":41100,\"long\":-58.4101627019175,\"lat\":-34.5583857696065,\"banco\":\"HSBC Bank Argentina\",\"red\":\"BANELCO\",\"ubicacion\":\"\",\"localidad\":\"CABA\",\"terminales\":2,\"no_vidente\":false,\"dolares\":false,\"calle\":\"\",\"altura\":0,\"calle2\":\"\",\"barrio\":\"\",\"comuna\":\"\",\"codigo_postal\":\"\",\"codigo_postal_argentino\":\"\"},{\"id\":40720,\"long\":-58.4084250406506,\"lat\":-34.5600587161855,\"banco\":\"BBVA Banco Francés\",\"red\":\"BANELCO\",\"ubicacion\":\"\",\"localidad\":\"CABA\",\"terminales\":1,\"no_vidente\":false,\"dolares\":false,\"calle\":\"\",\"altura\":0,\"calle2\":\"\",\"barrio\":\"\",\"comuna\":\"\",\"codigo_postal\":\"\",\"codigo_postal_argentino\":\"\"}]";
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Atm[] atms = mapper.readValue(response, Atm[].class);
        assertEquals(2, atms.length);
    }
}