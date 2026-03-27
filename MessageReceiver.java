//client side
import java.io.*;
import java.net.*;
import java.util.*;

public class MessageReceiver implements Runnable {
    private Socket socket;
    private ObjectOutputStream out;

    public MessageReceiver(Socket socket, ObjectOutputStream out) {
        this.socket = socket;
        this.out = out;
    }

    @Override
    public void run() {
        try {
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            while (true) {
                Message message = (Message) in.readObject();

                if (message.Username.equals("SERVER")) {

                    if (message.content.startsWith("Invalid")){
                        System.out.println(message.content);
                        continue;
                    }
                    if(message.content.startsWith("COORDINATOR:")){
                        String coord = message.content.substring("COORDINATOR:".length());
                        System.out.println("The current coordinator is: " + coord);
                        continue;
                    }

                    //member left — update member list
                    if (message.content.startsWith("MEMBER_LEFT:")) {
                        String[] parts = message.content.split(":", 3);
                        String leftID   = parts[1];
                        String leftName = parts[2];
                        System.out.println("<-- " + leftName + " (" + leftID + ") has left.");
                        continue;
                    }

                    //ping — reply pong so server knows we are alive
                    if (message.content.equals("PING")) {
                        out.writeObject(new Message("CLIENT", "PONG"));
                        out.flush();
                        continue;
                    }

                    // default: must be the ID assignment message
                    System.out.println("Your unique ID: " + message.content);
                    continue;
                }

                if (message.content.startsWith("Private Message:")){
                    System.out.println("[PRIVATE] " + message.Username + ": " + message.content.substring(16));
                    continue;
                }

                System.out.println(message);
            }
        }
        catch (IOException | ClassNotFoundException e){
            System.out.println("Disconnected from the Server");
        }
    }
}