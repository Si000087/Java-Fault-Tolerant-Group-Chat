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
            Server.clientUsernames.put(assignedID, DisplayUsername);

  
            //tell client their ID
            Message idMsg = new Message("SERVER", assignedID);
            out.writeObject(idMsg);
            out.flush();
            //tell client who coordinator is
            Message CoordMsg = new Message("SERVER", "COORDINATOR" +  Server.idCoordinator); 
            out.writeObject(CoordMsg);
            out.flush();

            //coordinator role features
            //takes ip and port from client and stores it
            String ip = socket.getInetAddress().getHostAddress();
            int port = socket.getPort();
            Server.clientIPs.put(assignedID,ip);
            Server.clientPorts.put(assignedID, port);
            
            //implementing loop
            boolean a=true;
            while (a){
                Message message = (Message)in.readObject();
                System.out.println(message);   
                //list
                if (message.content.equals("/list")){
                    for (String id : Server.clients.keySet()){
                        String ClientIP = Server.clientIPs.get(id);
                        int ClientPort = Server.clientPorts.get(id);
                        String ClientName = Server.clientUsernames.get(id); 
                        this.sendMessage(new Message("SERVER", "MEMBER:"+ id+ ":" + ClientName + ":" + ClientIP + ":" + ClientPort));
                    }
                    this.sendMessage(new Message("SERVER", "COORDINATOR:" + Server.idCoordinator));
                    continue;
                    
                }


                //private messaging

                if (message.content.startsWith("PRIVATE:")){
                    String[] parts = message.content.split(":",3);
                    String targetID = parts[1];
                    String PrivateMessage = parts[2];

                    ClientHandler target = clients.get(targetID);
                    if (target == null){
                        this.sendMessage(new Message("SERVER", "Invalid target ID: " + targetID));
                        continue;
                    }    
                        target.sendMessage(new Message(message.Username, "Sent you a private message:" + PrivateMessage));
                        continue;
                    
                }
                //Broadcast messaging
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
