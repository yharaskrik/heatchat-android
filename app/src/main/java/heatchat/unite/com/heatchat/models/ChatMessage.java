package heatchat.unite.com.heatchat.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class ChatMessage {

    private String text;
    private String uid;
    private long time;
    private double lat;
    private double lon;

    public ChatMessage(){

    }

    public ChatMessage(String uid, String text, double lat, double lon) {
        this.text = text;
        this.uid = uid;
        this.lon = lon;
        this.lat = lat;

        // Initialize to current time
        this.time = new Date().getTime() / 1000;
    }

    public ChatMessage(String uid, String text, double lat, double lon, long time) {
        this.text = text;
        this.uid = uid;
        this.lon = lon;
        this.lat = lat;
        this.time = time;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("text", text);
        result.put("time", time);
        result.put("lat", lat);
        result.put("lon", lon);

        return result;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
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
}