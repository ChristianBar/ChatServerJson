package chatserverjson;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientThread extends Thread {
    private Socket clientSocket;
    private BufferedReader reader;
    private PrintWriter writer;
    private ArrayList<ClientThread> clients;

    public ClientThread(Socket socket, ArrayList<ClientThread> clients) {
        this.clientSocket = socket;
        this.clients = clients;
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
            String message;
            while ((message = reader.readLine()) != null) {
                System.out.println("Ricevuto messaggio da " + clientSocket + ": " + message);
                broadcast(message);
            }
        } catch (IOException e) {
            System.out.println("ERRORE: " + e.getMessage());
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

    public void sendMessage(String message) {
        writer.println(message);
    }
    
    private void broadcast(String message) {
        System.out.println("Inviando a tutti: " + message);
        for (ClientThread client : clients) {
            client.sendMessage(message);
        }
    }

}
