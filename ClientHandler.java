import java.io.*;
import java.net.*;
import java.util.*;

public class ClientHandler implements Runnable{
    private Socket socket;
    //importing hashmap
    private HashMap<String, ClientHandler> clients;
    public ClientHandler(Socket socket, HashMap<String, ClientHandler> clients){
    this.socket = socket;
    this.clients = clients;
    }
    //bradcast message to users 
    //wip
    public void sendMessage (Message message){
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(message);
        }
        catch(IOException e){
        System.out.println("Error" + e.getMessage());
        }
    }

    @Override  

    public void run(){

        Message firstMessage =null;
        try {
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            //firstMessage records teh users Unique id only once, so whnever the code runs the uid wont get cloned
            firstMessage = (Message)in.readObject();
            clients.put(firstMessage.UID, this);
            //prints out the first ever message
            
            System.out.println("Clients connectected: "+ clients + ", ");
            System.out.println(firstMessage);
            
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
            System.out.println( firstMessage.UID + " disconnected from the Server");

        }
        //error detection
        catch (IOException | ClassNotFoundException e) {
    
        }
            
    }
}
