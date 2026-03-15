import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable{
    private Socket socket;
    public ClientHandler(Socket socket){
    this.socket = socket;
    }
    @Override
    public void run(){
        try {
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            Message message = (Message)in.readObject();
            System.out.println(message);         
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error" + e.getMessage());
        }
        
    }
}
