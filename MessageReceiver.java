import java.io.*;
import java.net.*;

public class MessageReceiver implements Runnable {
    private Socket socket;
    
    public MessageReceiver(Socket socket) {
        this.socket = socket; 
    }
    
    @Override
    public void run() {
        try {
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            while (true) {
                Message message = (Message) in.readObject();
                System.out.println(message);
            }
        }
        catch (IOException | ClassNotFoundException e){
            System.out.println("disconnected from the Server");
        }

}
}
