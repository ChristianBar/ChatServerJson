package chatserverjson;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class ClientThread extends Thread {
    private Socket clientSocket;
    private BufferedReader reader;
    private PrintWriter writer;
    private ChatData data;
    private String name;

    public ClientThread(Socket socket, ChatData data) {
        this.clientSocket = socket;
        this.data = data;
        try {
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println("ERRORE: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            String message = reader.readLine();

            JSONObject firstObj = new JSONObject(message);
            JSONArray firstMessage = firstObj.getJSONArray("messages");
            ChatData.getMessages().put(firstMessage.getJSONObject(0));
            JSONObject firstName = firstMessage.getJSONObject(0);
            name = firstName.getString("name");

            System.out.println(name + " si è connesso da " + clientSocket);
            
            while ((message = reader.readLine()) != null) {
                
                JSONObject obj = new JSONObject(message);
                JSONArray incomingMessages = obj.optJSONArray("messages", new JSONArray());
                
                for(int i=0; i<incomingMessages.length(); i++)
                    ChatData.getMessages().put(incomingMessages.get(i));
                
                System.out.println("Ricevuto messaggio da " + clientSocket + ": " + message);
                broadcast(message);
            }
        } catch (IOException | InterruptedException e) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                clientSocket.close();
                while(data.isLocked()) Thread.sleep(10);
                data.setLocked(true);
                data.getClients().remove(this);
                data.setLocked(false);
                
                JSONObject obj = new JSONObject();
                obj.put("messages", ChatData.getMessages());
                
                JSONArray clientsArray = new JSONArray();
                for (ClientThread client : ChatData.getClients()) {
                    JSONObject userObj = new JSONObject();
                    userObj.put("name", client.getUserName());
                    clientsArray.put(userObj);
                }
                obj.put("users", clientsArray);
                
                broadcast(obj.toString());
                
                System.out.println("Connessione chiusa: " + clientSocket);
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public synchronized void sendMessage(String message) {
        writer.println(message);
    }
    
    public synchronized void broadcast(String message) throws InterruptedException {
        while(data.isLocked()) Thread.sleep(10);
        data.setLocked(true);
        System.out.println("Inviando a tutti: " + message);
        for (ClientThread client : data.getClients()) {
            client.sendMessage(message);
        }
        data.setLocked(false);
    }
    
    public String getUserName() {
        return name;
    }
}
