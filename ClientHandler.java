import java.io.*;
import java.net.*;
import java.util.*;

public class ClientHandler implements Runnable{
    private Socket socket;
    private ObjectOutputStream out;
    //importing hashmap
    private HashMap<String, ClientHandler> clients;
    private String assignedID;
    public ClientHandler(Socket socket, HashMap<String, ClientHandler> clients, String assignedID){
    this.socket = socket;
    this.clients = clients;
    this.assignedID = assignedID;
    }
    //bradcast message to users 
    //wip
    public void sendMessage (Message message){
        try {
            out.writeObject(message);
            out.flush();
            
            
        }
        catch(IOException e){
        System.out.println("Error" + e.getMessage());
        }
    }

    @Override  

    public void run(){

        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());           
            out.flush();
        
            Message UsernameMessage = (Message) in.readObject();
            String DisplayUsername = UsernameMessage.Username; 
            System.out.println("Client joined with username: " + DisplayUsername);
            System.out.println(DisplayUsername + " assigned with ID: " + assignedID + "\n");
            clients.put(assignedID, this);
            
            
            //implementing loop
            boolean a=true;
            while (a){
                Message message = (Message)in.readObject();
                System.out.println(message);                
                for (ClientHandler client : clients.values()){
                    client.sendMessage(message);
                }
            }
            }
        //disconnection
        catch (SocketException e){
            System.out.println(  assignedID + " disconnected from the Server");

        }
        //error detection
        catch (IOException | ClassNotFoundException e) {
    
        }
            
    }
}
