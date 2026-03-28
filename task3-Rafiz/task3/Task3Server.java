package task3;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Task3Server {
    private Socket socket;
    private ServerSocket server;
    private DataInputStream in;

    public Task3Server(int port) {
        try {
            server = new ServerSocket(port);
            System.out.println("Server started");
            System.out.println("Waiting for a client ...");

            socket = server.accept();
            System.out.println("Client accepted");

            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

            String line = "";
            while (!line.equals("Stop")) {
                try {
                    line = in.readUTF();
                    System.out.println(line);
                } catch (IOException e) {
                    System.out.println(e);
                }
            }

            System.out.println("Closing connection");
            socket.close();
            in.close();
        } catch (IOException e) {
            System.out.println(e);
        }

        System.out.println("closing connection");
    }

    public static void main(String[] args) {
        new Task3Server(6666);
    }
}