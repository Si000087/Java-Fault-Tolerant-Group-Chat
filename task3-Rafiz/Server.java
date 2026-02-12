import java.io.*;
import java.net.*;

public class Server {
    //initialise socket and input stream

    private Socket socket;
    private ServerSocket server;
    private DataInputStream in;

    //contructor
    public Server(int port)
    {
        try
        {
            server= new ServerSocket(port);
            System.out.println("Server started");

            System.out.println("Waiting for a client ...");


            socket = server.accept();
            System.out.println("Client accepted");

            in = new DataInputStream(
                new BufferedInputStream(socket.getInputStream()));

            String line = "";

            //
            while (!line.equals ("Stop"))
            {
                try
                {
                    line = in.readUTF();
                    System.out.println(line);
                }
                catch(IOException i){
                    System.out.println(i);
                }
            }
            System.out.println("Closing connection");
            //close connection
            
            socket.close();
            in.close();
        }
        catch(IOException i){
            System.out.println(i);
        }
        System.out.println("closing connection");
}

public static void main(String[] args){
    Server server = new Server(6666);
}
}