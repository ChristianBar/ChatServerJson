package chatserverjson;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;

/**
 * Salva i log ogni tot secondi
 */
public class ChatLogger extends Thread {
    private ChatMessages allMessages;
    
    public ChatLogger(ChatMessages messages) {
        allMessages = messages;
    }
    
    @Override
    public void run() {
        try {
            do {
                Thread.sleep(1000);
                while(allMessages.isLocked()) Thread.sleep(100);
                
                allMessages.setLocked(true);
                String buffer = ChatMessages.getMessages().toString(4);
                BufferedWriter bw = new BufferedWriter(new FileWriter("messages.log", false));
                bw.write(buffer);
                bw.close();
                allMessages.setLocked(false);
            } while(true);
        } catch (InterruptedException ex) {
            Logger.getLogger(ChatLogger.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ChatLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
