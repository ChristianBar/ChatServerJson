package chatserverjson;

import java.util.ArrayList;
import org.json.JSONArray;

public class ChatData {
    private static ArrayList<ClientThread> clients = new ArrayList<>();
    private static JSONArray messages = new JSONArray();
    private boolean locked;

    public static JSONArray getMessages() {
        return messages;
    }

    public static ArrayList<ClientThread> getClients() {
        return clients;
    }
    
    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
