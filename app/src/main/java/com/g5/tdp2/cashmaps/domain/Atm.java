package com.g5.tdp2.cashmaps.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Entidad o punto con cajeros automaticos
 */
public class Atm {
    //Id, Latitud, Longitud, Direccion, Banco, Red y Candidad de terminales
    private long id;
    private double lat;
    private double lon;
    private String address;
    private AtmNet net;
    private String bank;
    private int terms;

    /**
     * Crea un punto de cajero
     *
     * @param id      Id de punto
     * @param lat     Latitud
     * @param lon     Longitud
     * @param address Direccion
     * @param net     Red [BANELCO|LINK]
     * @param bank    Banco
     * @param terms   Cantidad de terminales
     */
    @JsonCreator
    public Atm(
            @JsonProperty("id") long id,
            @JsonProperty("lat") double lat,
            @JsonProperty("long") double lon,
            @JsonProperty("ubicacion") String address,
            @JsonProperty("red") AtmNet net,
            @JsonProperty("banco") String bank,
            @JsonProperty("terminales") int terms) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.address = address;
        this.net = net;
        this.bank = bank;
        this.terms = terms;
    }

    public long getId() {
        return id;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getAddress() {
        return address;
    }

    public AtmNet getNet() {
        return net;
    }

    public String getBank() {
        return bank;
    }

    /**
     * Cantidad de terminales
     */
    public int getTerms() {
        return terms;
    }

    private static Stream<Atm> filterAtm(Stream<Atm> atms, AtmNet net) {
        return atms.filter(a -> net.equals(a.net));
    }

    private static Stream<Atm> filterBank(Stream<Atm> atms, String bank) {
        return atms.filter(a -> bank.equals(a.bank));
    }

    /**
     * Filtra un conjunto de cajeros unicamente por red y banco
     *
     * @param atms Cajeros a filtrar
     * @param net  Red
     * @param bank Banco
     * @return Cajeros filtrados
     */
    public static List<Atm> filter(List<Atm> atms, AtmNet net, String bank) {
        Stream<Atm> atmStream = atms.stream();
        return filterBank(filterAtm(atmStream, net), bank).collect(Collectors.toList());
    }

    /**
     * Verifica que un punto este dentro de la distancia especificada.
     *
     * @param myLat  Latitud
     * @param myLon  Longitud
     * @param radius Radio
     * @return True si el punto esta dentro de la distancia
     */
    public boolean withinDist(double myLat, double myLon, double radius) {
        return AtmDist.distanceMts(myLat, myLon, lat, lon) < radius;
    }

    /**
     * Filtra todos los cajeros de una red y un banco especificados que esten a una distancia de radio indicada
     *
     * @param atms   Lista de atms
     * @param net    Red
     * @param bank   Banco
     * @param myLat  Mi latitud
     * @param myLon  Mi longitud
     * @param radius Radio
     * @return Lista con cajeros que cumplen con los criterios definidos
     */
    public static List<Atm> filter(List<Atm> atms, AtmNet net, String bank, double myLat, double myLon, double radius) {
        List<Atm> filtered = filter(atms, net, bank);
        return filtered.stream()
                .filter(a -> a.withinDist(myLat, myLon, radius))
                .collect(Collectors.toList());
    }

    /**
     * Filtra todos los cajeros de una red y radio especificados
     *
     * @param atms   Lista de atms
     * @param net    Red
     * @param myLat  Mi latitud
     * @param myLon  Mi longitud
     * @param radius Radio
     * @return Lista con cajeros que cumplen con los criterios definidos
     */
    public static List<Atm> filter(List<Atm> atms, AtmNet net, double myLat, double myLon, double radius) {
        return atms.stream()
                .filter(a -> net.equals(a.net))
                .filter(a -> a.withinDist(myLat, myLon, radius))
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "Atm{" +
                "id=" + id +
                ", lat=" + lat +
                ", lon=" + lon +
                ", address='" + address + '\'' +
                ", net=" + net +
                ", bank='" + bank + '\'' +
                ", terms=" + terms +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Atm atm = (Atm) o;

        if (id != atm.id) return false;
        if (Double.compare(atm.lat, lat) != 0) return false;
        if (Double.compare(atm.lon, lon) != 0) return false;
        if (terms != atm.terms) return false;
        if (address != null ? !address.equals(atm.address) : atm.address != null) return false;
        if (net != atm.net) return false;
        return bank != null ? bank.equals(atm.bank) : atm.bank == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (int) (id ^ (id >>> 32));
        temp = Double.doubleToLongBits(lat);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(lon);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (net != null ? net.hashCode() : 0);
        result = 31 * result + (bank != null ? bank.hashCode() : 0);
        result = 31 * result + terms;
        return result;
    }
}
