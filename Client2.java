// basic client socket test
import java.io.*;
import java.net.*;
import java.util.Scanner;


public class Client2{
    
    public static void main(String[] args) { 
    
  
        try {
            Socket socket = new Socket("172.20.12.115",12345);
            System.out.println("Connected to Server");
            //sending output
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            MessageReceiver receiver = new MessageReceiver(socket);
            new Thread(receiver).start();
            //saving clients's username;
            System.out.println("Enter your name:\n");
            Scanner myName = new Scanner(System.in);
            String Username= myName.nextLine();
            System.out.println("Welcome: " + Username + "\nEnter your message:\n");
            
            
            
            //implemented loop
            boolean a= true;
            while (a){
                //sending custom message
                Scanner myObj = new Scanner(System.in);
                String myMessage= myObj.nextLine();
                if(myMessage.equals("quit")){
                    a = false;
                    System.out.println("Disconnecting");
                    break;
                }
                out.writeObject(new Message( Username , myMessage ));
                System.out.println("Enter your message:\n");
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
