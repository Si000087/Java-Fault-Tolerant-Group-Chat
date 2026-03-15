// basic client socket test
import java.io.*;
import java.net.*;


public class Client{
    
    public static void main(String[] args) { 
    
  
        try {
            Socket socket = new Socket("192.168.0.249",12345);
            System.out.println("Connected to Server");
            //sending output
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(new Message("Alice", "Hello Server"));
            //
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