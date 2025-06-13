package com.eles.smschecking;

import com.google.gson.annotations.SerializedName;

public class Route {
    @SerializedName("geometry")
    private String geometry;

    public String getGeometry() {
        return geometry;
    }

    public void setGeometry(String geometry) {
        this.geometry = geometry;
    }
}
