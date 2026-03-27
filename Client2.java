// basic client socket test
import java.io.*;
import java.net.*;
import java.util.Scanner;


public class Client2{
    
    public static void main(String[] args) { 
    
  
        try {
            Socket socket = new Socket("192.168.0.249",12345);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            
            //saving clients's username;
            System.out.println("Enter your name:\n");
            Scanner myName = new Scanner(System.in);
            String Username= myName.nextLine();
            System.out.println("Connected to Server");
            System.out.println("Welcome: " + Username);
            out.writeObject(new Message(Username, "JOIN"));
            out.flush();
            MessageReceiver receiver = new MessageReceiver(socket);
            new Thread(receiver).start();
            
            
            
            //implemented loop
            boolean a= true;
            while (a){
                //sending custom message
                Scanner myObj = new Scanner(System.in);
                String myMessage= myObj.nextLine();
                //private messaging
                if(myMessage.equals("/Private")||myMessage.equals("/private")){
                    //creating new inputs to read target and content without passing by the
                    // Message object arguments so it doesnt trigger a broadcast message
                    System.out.println("Enter target ID: ");
                    String TargetUID = myObj.nextLine();
                    System.out.println("Enter message:");
                    String PrivateMessage=myObj.nextLine(); 
                    String encoded = "PRIVATE:" + TargetUID + ": " + PrivateMessage;
                    out.writeObject(new Message(Username,encoded));
                        out.flush();
                        System.out.println("Message sent:");
                        continue;
                    

                }
                if(myMessage.equals("quit")){
                    a = false;
                    System.out.println("Disconnecting");
                    break;
                }
                out.writeObject(new Message( Username , myMessage));
                out.flush();
                
                
                
            }
            
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Error: " + e.getMessage());
            }

            
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            
        }
    }
}
