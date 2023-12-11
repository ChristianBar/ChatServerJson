package chatserverjson;

import org.json.JSONArray;


public class ChatMessages {
    private static JSONArray messages = new JSONArray();
    private boolean locked;

    public static JSONArray getMessages() {
        return messages;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
