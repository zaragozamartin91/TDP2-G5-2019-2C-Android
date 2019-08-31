package com.g5.tdp2.cashmaps.gateway.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.g5.tdp2.cashmaps.domain.AtmBank;
import com.g5.tdp2.cashmaps.gateway.AtmGatewayException;
import com.g5.tdp2.cashmaps.gateway.BankGateway;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class WebBankGateway implements BankGateway {
    private String baseUrl;

    public WebBankGateway() {
        this("https://tdp2-atm-api.herokuapp.com");
    }

    public WebBankGateway(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public List<String> getBanks() {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(baseUrl + "/bancos");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String response = getResponse(reader);
                return handleResponse(response);
            }
        } catch (IOException e) {
            throw new AtmGatewayException("Error en la red al obtener Bancos", e);
        } catch (IllegalArgumentException e) {
            throw new AtmGatewayException("Error al parsear respuesta de Bancos", e);
        } finally {
            Optional.ofNullable(connection).ifPresent(HttpURLConnection::disconnect);
        }
    }

    private String getResponse(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        return line == null ? "" : line.concat(getResponse(reader));
    }

    private List<String> handleResponse(String response) throws IllegalArgumentException {
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            String[] banks = mapper.readValue(response, String[].class);
            return AtmBank.INSTANCE.sort(Arrays.asList(banks));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
