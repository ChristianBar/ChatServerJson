package chatserverjson;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import org.json.JSONArray;

public class ChatServerJson {
    private static final int PORT = 12345;
    private static ArrayList<ClientThread> clients = new ArrayList<>();

    public static void main(String[] args) {
        try {
            // Creo il socket di accettazione
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server avviato sulla porta " + PORT);
            
            ChatMessages messages = new ChatMessages();
            
            // Avvio il logger
            ChatLogger logger = new ChatLogger(messages);
            logger.start();

            while (true) {
                // Accetto una nuova connessione
                Socket clientSocket = serverSocket.accept();
                
                // Avvio il thread che gestirà la nuova connessione
                ClientThread clientThread = new ClientThread(clientSocket, clients, messages);
                clients.add(clientThread);
                clientThread.start();

                // Avvio i messaggi vecchi
                clientThread.sendMessage(ChatMessages.getMessages().toString());
            }
        } catch (IOException e) {
            System.out.println("ERRORE: " + e.getMessage());
        }
    }
}
