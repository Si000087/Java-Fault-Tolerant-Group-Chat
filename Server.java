import java.io.*;
import java.net.*;
import java.util.*;
public class Server{
    //creating a hashmap to record the users that joined
    static HashMap<String , ClientHandler> clients = new HashMap<>();
    
    public static void main(String[] args){
        try {
            ServerSocket serversocket = new ServerSocket(12345);                
            System.out.println("Starting Server"); 
            System.out.println("Waiting for users to connect...\n");
            while(true) {
                Socket socket = serversocket.accept();
                ClientHandler handler = new ClientHandler(socket,clients);
                new Thread(handler).start();
                
                System.out.println("Connected");
            }
             
        } catch (IOException e) {
            System.out.println("Error" + e.getMessage());

        }

    }
}
