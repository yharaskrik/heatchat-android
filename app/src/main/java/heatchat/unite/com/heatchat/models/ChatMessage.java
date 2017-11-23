package heatchat.unite.com.heatchat.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@IgnoreExtraProperties
@Entity(tableName = "chatmessage")
public class ChatMessage implements Comparable<ChatMessage> {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "messageID")
    private String messageID;

    @ColumnInfo(name = "uid")
    private String uid;

    @ColumnInfo(name = "text")
    private String text;

    @ColumnInfo(name = "time")
    private long time;

    @ColumnInfo(name = "lat")
    private double lat;

    @ColumnInfo(name = "lon")
    private double lon;

    @ColumnInfo(name = "path")
    private String path;

    public ChatMessage() {

    }

    public ChatMessage(String uid, String text, double lat, double lon) {
        this.text = text;
        this.uid = uid;
        this.lon = lon;
        this.lat = lat;

        // Initialize to current time
        this.time = new Date().getTime();
    }

    public ChatMessage(String uid, String text, double lat, double lon, String path, String messageID) {
        this.text = text;
        this.uid = uid;
        this.lon = lon;
        this.lat = lat;
        this.path = path;
        this.messageID = messageID;

        // Initialize to current time
        this.time = new Date().getTime();
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

    @Override
    public boolean equals(Object obj) {

        if (obj == null)
            return false;
        else if (((ChatMessage) obj).messageID.equals(this.messageID))
            return true;
        else
            return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.messageID);
    }

    @Override
    public String toString() {
        return this.getMessageID();
    }

    @Override
    public int compareTo(ChatMessage message) {

        return (int) (this.getTime() - message.getTime());

    }

    @NonNull
    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(@NonNull String messageID) {
        this.messageID = messageID;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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