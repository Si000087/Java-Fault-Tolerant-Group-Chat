import java.io.*;
import java.net.*;

public class Server {
    public static void main(String[] args) {
        try (ServerSocket serversocket = new ServerSocket(12345)) { // try-with-resources fixes resource leak
            System.out.println("Starting Server");
            System.out.println("Waiting for users to connect...\n");
            ClientHandler.startPingLoop();
            while (true) {
                Socket socket = serversocket.accept();
                String assignedID = "C" + ChatServerState.idCounter;
                if (ChatServerState.idCoordinator == null) {
                    ChatServerState.idCoordinator = assignedID;
                }
                ChatServerState.idCounter++;
                ClientHandler handler = new ClientHandler(socket, ChatServerState.clients, assignedID);
                ChatServerState.clients.put(assignedID, handler);
                new Thread(handler).start();
                System.out.println("Socket Connected: " + assignedID);
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
