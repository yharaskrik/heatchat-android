package heatchat.unite.com.heatchat.navDrawer;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by jaybell on 05/11/17.
 */

@IgnoreExtraProperties
public class School {

    private String name;
    private double lat, lon, distance;
    private String path;

    School() {}

    School(String name, double lat, double lon, String path) {
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
