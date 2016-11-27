package com.ankitgupta.android.sunshine.model;

/**
 * Created by ankit.gupta on 11/27/16.
 */

public class OpenWeatherAPIParams {


    String apiId;
    String postalCode;
    String mode;
    String units;
    String cnt;
    String lang;

    public OpenWeatherAPIParams(String apiId, String postalCode) {
        this.apiId = apiId;
        this.postalCode = postalCode;
    }

    public String getApiId() {
        return apiId;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getCnt() {
        return cnt;
    }

    public void setCnt(String cnt) {
        this.cnt = cnt;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }
}
