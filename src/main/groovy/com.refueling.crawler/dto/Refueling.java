package com.refueling.crawler.dto;

import org.joda.time.DateTime;

public class Refueling {

    private String accountNr;
    private String cardNr;
    private String cardOwner;
    private DateTime refuelingDate;
    private String stationName;
    private String product;
    private float numberOfLitres;
    private float pricePerLitre;
    private String discount;

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public float getNumberOfLitres() {
        return numberOfLitres;
    }

    public void setNumberOfLitres(float numberOfLitres) {
        this.numberOfLitres = numberOfLitres;
    }

    public float getPricePerLitre() {
        return pricePerLitre;
    }

    public void setPricePerLitre(float pricePerLitre) {
        this.pricePerLitre = pricePerLitre;
    }

    public DateTime getRefuelingDate() {
        return refuelingDate;
    }

    public void setRefuelingDate(DateTime refuelingDate) {
        this.refuelingDate = refuelingDate;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getAccountNr() {
        return accountNr;
    }

    public void setAccountNr(String accountNr) {
        this.accountNr = accountNr;
    }

    @Override
    public String toString() {
        return getAccountNr() + ", " + getRefuelingDate() + ", " + getStationName() + ", " + getNumberOfLitres();
    }
}
