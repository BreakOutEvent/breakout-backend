package backend.controller.RequestBodies;

import javax.validation.constraints.NotNull;

public class Location {

    @NotNull
    private Double lat;

    @NotNull
    private Double lon;

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }
}
