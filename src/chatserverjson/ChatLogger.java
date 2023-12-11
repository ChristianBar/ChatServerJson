package chatserverjson;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Salva il log dei messaggi ogni tot secondi
 */
public class ChatLogger extends Thread {
    private ChatData data;
    
    public ChatLogger(ChatData messages) {
        data = messages;
    }
    
    @Override
    public void run() {
        try {
            do {
                Thread.sleep(1000);
                while(data.isLocked()) Thread.sleep(1000);
                
                data.setLocked(true);
                String buffer = ChatData.getMessages().toString(4);
                BufferedWriter bw = new BufferedWriter(new FileWriter("messages.log", false));
                bw.write(buffer);
                bw.close();
                data.setLocked(false);
            } while(true);
        } catch (InterruptedException | IOException ex) {
            Logger.getLogger(ChatLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
