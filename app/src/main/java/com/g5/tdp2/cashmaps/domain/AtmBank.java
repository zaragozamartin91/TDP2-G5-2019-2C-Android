package com.g5.tdp2.cashmaps.domain;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public enum AtmBank {
    INSTANCE;

    /**
     * Normaliza y ordena nombres de bancos
     *
     * @param banks Nombres de bancos
     * @return Nomrbes de bancos normalizados y ordenados alfabeticamente
     */
    public List<String> sort(List<String> banks) {
        return banks.stream()
                .filter(Objects::nonNull)
                .sorted(String::compareTo)
                .collect(Collectors.toList());
    }
}
