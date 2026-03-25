import java.io.*;
import java.net.*;
import java.util.*;
public class Server{
    //creating a hashmap to record the users that joined
    static HashMap<String , ClientHandler> clients = new HashMap<>();
    public static int idCounter =1;
    

    

    public static void main(String[] args){
        try {
            ServerSocket serversocket = new ServerSocket(12345);                
            System.out.println("Starting Server"); 
            System.out.println("Waiting for users to connect...\n");
            while(true) {
                Socket socket = serversocket.accept();
                String assignedID = "C" + idCounter;
                idCounter++;
                ClientHandler handler = new ClientHandler(socket,clients,assignedID);
                clients.put(assignedID, handler);
                new Thread(handler).start();
                
                System.out.println("Connected" + assignedID);
            }
             
        } catch (IOException e) {
            System.out.println("Error" + e.getMessage());

        }
    

    }
}
