package com.eles.smschecking;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RouteResponse {
    @SerializedName("routes")
    private List<Route> routes;

    public List<Route> getRoutes() {
        return routes;
    }

    public static class Route {
        @SerializedName("geometry")
        private Geometry geometry;

        public Geometry getGeometry() {
            return geometry;
        }
    }

    public static class Geometry {
        @SerializedName("coordinates")
        private List<List<Double>> coordinates;

        public List<List<Double>> getCoordinates() {
            return coordinates;
        }
    }
}
