import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    // client handler map
    static HashMap<String, ClientHandler> clients = new HashMap<>();
    // from shani: store usernames, IPs, ports for /list command
    static HashMap<String, String> clientUsernames = new HashMap<>();
    static HashMap<String, String> clientIPs = new HashMap<>();
    static HashMap<String, Integer> clientPorts = new HashMap<>();

    public static int idCounter = 1;
    public static String idCoordinator = null;

    public static void main(String[] args) {
        try (ServerSocket serversocket = new ServerSocket(12345)) { // try-with-resources fixes resource leak
            System.out.println("Starting Server");
            System.out.println("Waiting for users to connect...\n");
            ClientHandler.startPingLoop(); // from main: fault-tolerance ping loop
            while (true) {
                Socket socket = serversocket.accept();
                String assignedID = "C" + idCounter;
                if (idCoordinator == null) {
                    idCoordinator = assignedID;
                    ClientHandler.idCoordinator = assignedID;
                }
                idCounter++;
                ClientHandler handler = new ClientHandler(socket, clients, assignedID);
                clients.put(assignedID, handler);
                ClientHandler.clientsStatic = clients;
                new Thread(handler).start();
                System.out.println("Socket Connected: " + assignedID);
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
