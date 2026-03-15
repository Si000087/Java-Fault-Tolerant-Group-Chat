import java.io.*;
import java.net.*;
public class Server{
    public static void main(String[] args){
        try {
            ServerSocket serversocket = new ServerSocket(12345);                
            System.out.println("Starting Server"); 
            while(true) {
                Socket socket = serversocket.accept();
                ClientHandler handler = new ClientHandler(socket);
                new Thread(handler).start();
                System.out.println("Connected");
            }
             
        } catch (IOException e) {
            System.out.println("Error" + e.getMessage());
        }

    }
}
