package chatserverjson;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

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
                ChatData.getClients().add(clientThread);
                data.setLocked(false);
               
                clientThread.start();
            }
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(ChatServerJson.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
