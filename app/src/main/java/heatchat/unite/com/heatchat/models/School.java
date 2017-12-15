package heatchat.unite.com.heatchat.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
@Entity(tableName = "school")
public class School implements Comparable<School> {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "name", index = true)
    private String name;

    @ColumnInfo(name = "lat")
    private double lat;
    @ColumnInfo(name = "lon")
    private double lon;

    @ColumnInfo(name = "path")
    private String path;

    @ColumnInfo(name = "radius")
    private int radius;

    private transient double distance;

    public School() {
        // Required empty args constructor
    }

    public School(@NonNull String name, double lat, double lon, String path, int radius) {
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.path = path;
        this.radius = radius;
    }

    @Override
    public int compareTo(@NonNull School school) {
        if (school.getDistance() == getDistance()) {
            return school.getName().compareTo(getName());
        } else if (school.getDistance() < getDistance()) {
            return 1;
        } else {
            return -1;
        }
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
    public String toString() {
        return "School{" +
                "name='" + name + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                ", path='" + path + '\'' +
                ", radius=" + radius +
                ", distance=" + distance +
                '}';
    }
}
