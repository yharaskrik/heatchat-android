package heatchat.unite.com.heatchat.models;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class School implements Comparable<School> {

    private String name;
    private double lat, lon, distance;
    private String path;
    private int radius;

    public School() {
    }

    public School(String name, double lat, double lon, String path) {
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.path = path;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("path", path);
        result.put("lat", lat);
        result.put("lon", lon);

        return result;
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

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    @Override
    public int compareTo(@NonNull School school) {
        if (school.getDistance() > this.getDistance())
            return 0;
        else
            return 1;
    }
}
