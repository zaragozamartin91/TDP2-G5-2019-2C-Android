package com.g5.tdp2.cashmaps.gateway.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.g5.tdp2.cashmaps.domain.Atm;
import com.g5.tdp2.cashmaps.gateway.AtmGateway;
import com.g5.tdp2.cashmaps.gateway.AtmGatewayException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class WebAtmGateway implements AtmGateway {
    private String baseUrl;

    public WebAtmGateway() {
        this("https://tdp2-atm-api.herokuapp.com");
    }

    public WebAtmGateway(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public List<Atm> getAtms(Request request) throws AtmGatewayException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(buildRequestUrl(request));
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String response = getResponse(reader);
                return handleResponse(response);
            }
        } catch (IOException e) {
            throw new AtmGatewayException("Error en la red al obtener ATMs", e);
        } catch (IllegalArgumentException e) {
            throw new AtmGatewayException("Error al parsear respuesta de ATMs", e);
        } finally {
            Optional.ofNullable(connection).ifPresent(HttpURLConnection::disconnect);
        }
    }

    private String buildRequestUrl(Request request) {
        String url = baseUrl + "/atms";
        return request.net == null ? url : // si no se indica la red, se solicitan todos los ATMs
                request.bank == null ? url + "?red=" + request.net : // si se indica la red pero no el banco, se filtra solo por red
                        url + "?red=" + request.net + "&banco=" + encodeValue(request.bank); // si ambos parametros existen -> se filtra por red y banco
    }

    private String getResponse(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        return line == null ? "" : line.concat(getResponse(reader));
    }

    private List<Atm> handleResponse(String response) throws IllegalArgumentException {
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            Atm[] atms = mapper.readValue(response, Atm[].class);
            return Arrays.asList(atms);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    // Method to encode a string value using `UTF-8` encoding scheme
    private static String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }
}
