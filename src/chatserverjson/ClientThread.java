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
            
            // Legge il nome dal primo messaggio (vuoto) ricevuto
            JSONObject firstObj = new JSONObject(message);
            JSONArray firstMessage = firstObj.getJSONArray("messages");
            JSONObject firstName = firstMessage.getJSONObject(0);
            name = firstName.getString("name");
            
            // Invia a tutti l'elenco degli utenti
            JSONObject objNames = new JSONObject();
            JSONArray clientsArray = new JSONArray();
            for (ClientThread client : ChatData.getClients()) {
                JSONObject userObj = new JSONObject();
                userObj.put("name", client.getUserName());
                clientsArray.put(userObj);
            }
            objNames.put("users", clientsArray);
            broadcast(objNames.toString());

            // Si auto-manda lo storico dei messaggi
            JSONObject obj2 = new JSONObject();
            obj2.put("messages", ChatData.getMessages());
            sendMessage(obj2.toString());
            
            System.out.println(name + " si è connesso da " + clientSocket);

            // Ciclo principale: riceve un messaggio e lo propaga a tutti dopo esserselo salvato
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
                // Chiude la connessione e si auto-elimina dall'elenco dei client
                clientSocket.close();
                while(data.isLocked()) Thread.sleep(10);
                data.setLocked(true);
                data.getClients().remove(this);
                data.setLocked(false);

                // manda l'elenco degli utenti aggiornato (meno se stesso, quindi)
                JSONObject obj = new JSONObject();
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

    // Invia un messaggio al proprio socket
    public synchronized void sendMessage(String message) {
        writer.println(message);
    }
    
    // Invia un messaggio a tutti i client connessi
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
