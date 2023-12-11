package chatserverjson;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class ChatServerJson {
    private static final int PORT = 12345;

    public static void main(String[] args) {
        try {
            // Creo il socket di accettazione
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server avviato sulla porta " + PORT);
            
            ChatData data = new ChatData();
            
            // Avvio il logger
            ChatLogger logger = new ChatLogger(data);
            logger.start();

            while (true) {
                // Accetto una nuova connessione
                Socket clientSocket = serverSocket.accept();
                
                // Avvio il thread che gestirà la nuova connessione
                ClientThread clientThread = new ClientThread(clientSocket, data);
                
                while(data.isLocked()) Thread.sleep(10);
                data.setLocked(true);
                data.getClients().add(clientThread);
                data.setLocked(false);
               
                clientThread.start();

                // Avvio i messaggi vecchi
                Thread.sleep(1000); // TODO: togliere
                JSONObject obj = new JSONObject();
                obj.put("messages", ChatData.getMessages());
                clientThread.sendMessage(obj.toString());
                
                obj = new JSONObject();
                obj.put("messages", new JSONArray());
                JSONArray clientsArray = new JSONArray();
                for (ClientThread client : ChatData.getClients()) {
                    JSONObject userObj = new JSONObject();
                    userObj.put("name", client.getUserName());
                    clientsArray.put(userObj);
                }
                obj.put("users", clientsArray);
                
                clientThread.broadcast(obj.toString());
            }
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(ChatServerJson.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
