//client side
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

                if (message.content.startsWith("MEMBER:")){
                    String[] parts = message.content.split(":");
                    String id = parts[1];
                    String name = parts[2];
                    String ip = parts[3];
                    String port = parts[4];
                    System.out.println("Member ID: " + id +", username: "+ name +", IP: "+ ip + ", port; " + port);
                    continue;
                }

                if (message.Username.equals("SERVER")) {
                    if (message.content.startsWith("Invalid")){
                        System.out.println(message.content);
                        continue;
                    }

                    if(message.content.startsWith("COORDINATOR")){
                        String coord = message.content.substring("COORDINATOR".length() + 1);
                            System.out.println("The current coordinator is: " + coord);
                            continue;
                    }
                    System.out.println("Your unique ID: " + message.content);
                    continue;  
                }


                
                if (message.content.startsWith("PRIVATE:")){
                    System.out.println("PRIVATE:" + message.Username + ": " + message.content.substring(10));
                    continue;
                }

                System.out.println(message);
                

            }
        }
        catch (IOException | ClassNotFoundException e){
            System.out.println("disconnected from the Server");
            e.printStackTrace();
        }

}
}
