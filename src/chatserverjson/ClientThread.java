package chatserverjson;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class ClientThread extends Thread {
    private Socket clientSocket;
    private BufferedReader reader;
    private PrintWriter writer;
    private ArrayList<ClientThread> clients;
    private ChatMessages allMessages;
    private String name;

    public ClientThread(Socket socket, ArrayList<ClientThread> clients, ChatMessages allMessages) {
        this.clientSocket = socket;
        this.clients = clients;
        this.allMessages = allMessages;
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
            JSONObject msg = new JSONObject(message);
            name = msg.get("name").toString();
            System.out.println(name + " si è connesso da " + clientSocket);
            
            while ((message = reader.readLine()) != null) {
                
                JSONArray incomingMessages = new JSONArray(message);
                for(int i=0; i<incomingMessages.length(); i++)
                    ChatMessages.getMessages().put(incomingMessages.get(i));
                
                System.out.println("Ricevuto messaggio da " + clientSocket + ": " + message);
                broadcast(message);
            }
        } catch (IOException | InterruptedException e) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                clientSocket.close();
                clients.remove(this);
                System.out.println("Connessione chiusa: " + clientSocket);
            } catch (IOException e) {
                System.out.println("ERRORE: " + e.getMessage());
            }
        }
    }

    public synchronized void sendMessage(String message) {
        writer.println(message);
    }
    
    private synchronized void broadcast(String message) throws InterruptedException {
        while(allMessages.isLocked()) Thread.sleep(10);
        allMessages.setLocked(true);
        System.out.println("Inviando a tutti: " + message);
        for (ClientThread client : clients) {
            client.sendMessage(message);
        }
        allMessages.setLocked(false);
    }
}
