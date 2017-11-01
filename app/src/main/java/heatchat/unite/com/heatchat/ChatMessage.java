package heatchat.unite.com.heatchat;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatMessage {

    private String messageText;
    private String messageUser;
    private long messageTime;
    private double latitude;
    private double longitude;

    public ChatMessage(String messageText, String messageUser, double latitude, double longitude) {
        this.messageText = messageText;
        this.messageUser = messageUser;
        this.longitude = longitude;
        this.latitude = latitude;

        // Initialize to current time
        this.messageTime = (long) (new Date().getTime() / 1000);
    }

    public ChatMessage(){

    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", messageUser);
        result.put("text", messageText);
        result.put("time", messageTime);
        result.put("lat", latitude);
        result.put("lon", longitude);

        return result;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageUser() {
        return messageUser;
    }

    public void setMessageUser(String messageUser) {
        this.messageUser = messageUser;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }
}