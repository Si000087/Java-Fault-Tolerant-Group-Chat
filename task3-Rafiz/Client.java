import java.io.*;
import java.net.*;


public class Client{
    //initialise port
    private Socket socket;
    private BufferedReader input;
    private DataOutputStream out;


public Client(String address, int port)
{
    //try to establish a connection
    try
    {
        socket = new Socket(address, port);
        System.out.println("Connected");


        //input buffer
        input = new BufferedReader(new InputStreamReader(System.in));
        //output
        out = new DataOutputStream(socket.getOutputStream());  
    }
    catch(UnknownHostException u){
        System.out.println(u);
    }
    catch(IOException i){
        System.out.println(i);
    }

    //read message
    String line = "";

    //keep reading until Stop
    while (!line.equals("Stop"))
    {
        try
        {
            line = input.readLine();

            out.writeUTF(line);
        }
        catch(IOException i)
        {
            System.out.println(i);
        }
    }

    //close connection
    try
    {
        input.close();
        out.close();
        socket.close();
    }
    catch(IOException i){
        System.out.println(i);
    }
}

public static void main(String args[]){
  Client client = new Client("127.0.0.1",6666);  
}

}
