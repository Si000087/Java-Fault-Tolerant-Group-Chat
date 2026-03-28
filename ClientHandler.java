import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectOutputStream out;
    private final ConcurrentMap<String, ClientHandler> clients;
    private String assignedID;
    private String displayUsername = "";
    public static final ConcurrentMap<String, ClientHandler> clientsStatic = ChatServerState.clients;
    final AtomicBoolean awaitingPong = new AtomicBoolean(false); // from main: ping/pong tracking

    public ClientHandler(Socket socket, ConcurrentMap<String, ClientHandler> clients, String assignedID) {
        this.socket = socket;
        this.clients = clients;
        this.assignedID = assignedID;
    }

    private String findClientIdByUsername(String username) {
        String matchedId = null;
        for (Map.Entry<String, String> entry : ChatServerState.clientUsernames.entrySet()) {
            if (!entry.getValue().equalsIgnoreCase(username)) {
                continue;
            }
            if (matchedId != null) {
                return null;
            }
            matchedId = entry.getKey();
        }
        return matchedId;
    }

    // broadcast message to a specific client
    public void sendMessage(Message message) {
        if (out == null) {
            return;
        }
        try {
            synchronized (out) {
                out.writeObject(message);
                out.flush();
            }
        } catch (IOException e) {
            System.out.println("Error sending to " + assignedID + ": " + e.getMessage());
        }
    }

    @Override
    public void run() {
        ObjectInputStream in = null;
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            out.flush();

            // read username from JOIN message
            Message UsernameMessage = (Message) in.readObject();
            displayUsername = UsernameMessage.Username;
            System.out.println("Client joined with username: " + displayUsername);
            System.out.println(displayUsername + " assigned with ID: " + assignedID + "\n");

            // from shani: store username, IP, port for /list command
            ChatServerState.clientUsernames.put(assignedID, displayUsername);
            String ip = socket.getInetAddress().getHostAddress();
            int port = socket.getPort();
            ChatServerState.clientIPs.put(assignedID, ip);
            ChatServerState.clientPorts.put(assignedID, port);

            // tell client their assigned ID
            sendMessage(new Message("SERVER", assignedID));

            // tell client who the coordinator is
            sendMessage(new Message("SERVER", "COORDINATOR:" + ChatServerState.idCoordinator));

            // main message loop
            while (true) {
                Message message = (Message) in.readObject();
                System.out.println(message);

                // graceful leave
                if (message.content.equals("LEAVE")) {
                    handleLeave(displayUsername);
                    return;
                }

                // from main: pong reply clears the ping flag
                if (message.content.equals("PONG")) {
                    awaitingPong.set(false);
                    continue;
                }

                // from shani: /list command — send all member details
                if (message.content.equals("/list")) {
                    for (String id : ChatServerState.clients.keySet()) {
                        String clientIP   = ChatServerState.clientIPs.get(id);
                        Integer clientPort = ChatServerState.clientPorts.get(id);
                        String clientName = ChatServerState.clientUsernames.get(id);
                        if (clientIP == null || clientPort == null || clientName == null) {
                            continue;
                        }
                        this.sendMessage(new Message("SERVER",
                            "MEMBER:" + id + ":" + clientName + ":" + clientIP + ":" + clientPort));
                    }
                    this.sendMessage(new Message("SERVER", "COORDINATOR:" + ChatServerState.idCoordinator));
                    continue;
                }

                // private messaging
                if (message.content.startsWith("PRIVATE:")) {
                    String[] parts = message.content.split(":", 3);
                    if (parts.length < 3) {
                        this.sendMessage(new Message("SERVER", "Invalid private message format."));
                        continue;
                    }
                    String targetInput = parts[1].trim();
                    String privateMsg = parts[2];
                    String targetID = targetInput;
                    ClientHandler target = clients.get(targetID);
                    if (target == null) {
                        targetID = findClientIdByUsername(targetInput);
                        if (targetID != null) {
                            target = clients.get(targetID);
                        }
                    }
                    if (target == null) {
                        this.sendMessage(new Message("SERVER", "Invalid or ambiguous target: " + targetInput));
                        continue;
                    }
                    target.sendMessage(new Message(message.Username, "Private Message:" + privateMsg));
                    continue;
                }

                // broadcast to all clients
                for (ClientHandler client : clients.values()) {
                    client.sendMessage(message);
                }
            }

        // socket drop — treat as failure
        } catch (SocketException e) {
            System.out.println(assignedID + " socket error: " + e.getMessage());
            handleFailure(displayUsername);
        } catch (IOException | ClassNotFoundException e) {
            handleFailure(displayUsername);
        } finally {
            try { if (in != null) in.close(); } catch (IOException ignored) {}
            try { if (out != null) out.close(); } catch (IOException ignored) {}
            try { if (socket != null && !socket.isClosed()) socket.close(); } catch (IOException ignored) {}
        }
    }

    // graceful leave: client sent LEAVE
    private void handleLeave(String displayUsername) {
        sendMessage(new Message("SERVER", "DISCONNECTED"));
        synchronized (clients) {
            if (!clients.containsKey(assignedID)) return;
            clients.remove(assignedID);
        }
        ChatServerState.clientUsernames.remove(assignedID);
        ChatServerState.clientIPs.remove(assignedID);
        ChatServerState.clientPorts.remove(assignedID);
        try { socket.close(); } catch (IOException ignored) {}
        System.out.println(assignedID + " (" + displayUsername + ") left gracefully.");
        broadcastToAllStatic(new Message("SERVER", "MEMBER_LEFT:" + assignedID + ":" + displayUsername));
        if (assignedID.equals(ChatServerState.idCoordinator)) {
            electCoordinator();
        }
    }

    // failure: missed ping or socket error
    private void handleFailure(String displayUsername) {
        String name = (displayUsername != null && !displayUsername.isEmpty()) ? displayUsername : this.displayUsername;
        if (name == null || name.isEmpty()) name = "<unknown>";
        synchronized (clients) {
            if (!clients.containsKey(assignedID)) return;
            clients.remove(assignedID);
        }
        ChatServerState.clientUsernames.remove(assignedID);
        ChatServerState.clientIPs.remove(assignedID);
        ChatServerState.clientPorts.remove(assignedID);
        try { socket.close(); } catch (IOException ignored) {}
        System.out.println(assignedID + " removed due to failure.");
        broadcastToAllStatic(new Message("SERVER", "MEMBER_LEFT:" + assignedID + ":" + name));
        if (assignedID.equals(ChatServerState.idCoordinator)) {
            electCoordinator();
        }
    }

    // elect the client with the lowest numeric ID as the new coordinator
    static void electCoordinator() {
        synchronized (ClientHandler.clientsStatic) {
            if (ClientHandler.clientsStatic.isEmpty()) {
                ChatServerState.idCoordinator = null;
                System.out.println("[Election] No members left.");
                return;
            }
            String newCoord = ClientHandler.clientsStatic.keySet().stream()
                .min(Comparator.comparingInt(id -> Integer.parseInt(id.substring(1))))
                .orElse(null);
            ChatServerState.idCoordinator = newCoord;
            System.out.println("[Election] New coordinator: " + newCoord);
            broadcastToAllStatic(new Message("SERVER", "COORDINATOR:" + newCoord));
        }
    }

    static void startPingLoop() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            List<ClientHandler> snapshot;
            synchronized (ClientHandler.clientsStatic) {
                snapshot = new ArrayList<>(ClientHandler.clientsStatic.values());
            }
            for (ClientHandler client : snapshot) {
                client.awaitingPong.set(true);
                client.sendMessage(new Message("SERVER", "PING"));
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            for (ClientHandler client : snapshot) {
                if (client.awaitingPong.get()) {
                    System.out.println("[Ping] " + client.assignedID + " missed PONG — removing.");
                    client.handleFailure(client.displayUsername);
                }
            }
        }, 5000, 5000, TimeUnit.MILLISECONDS);
        System.out.println("[Ping] Loop started (5s interval, 3s timeout).");
    }

    static void broadcastToAllStatic(Message msg) {
        synchronized (ClientHandler.clientsStatic) {
            for (ClientHandler c : ClientHandler.clientsStatic.values()) {
                c.sendMessage(msg);
            }
        }
    }
}
