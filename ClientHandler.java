import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class ClientHandler implements Runnable{
    private Socket socket;
    private ObjectOutputStream out;
    private HashMap<String, ClientHandler> clients;
    private String assignedID;
    final AtomicBoolean awaitingPong = new AtomicBoolean(false);

    public ClientHandler(Socket socket, HashMap<String, ClientHandler> clients, String assignedID){
        this.socket = socket;
        this.clients = clients;
        this.assignedID = assignedID;
    }

    //broadcast message to users
    public void sendMessage (Message message){
        try {
            out.writeObject(message);
            out.flush();
        }
        catch(IOException e){
            System.out.println("Error" + e.getMessage());
        }
    }

    @Override
    public void run(){
        String displayUsername = "";
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            out.flush();

            Message UsernameMessage = (Message) in.readObject();
            displayUsername = UsernameMessage.Username;
            System.out.println("Client joined with username: " + displayUsername);
            System.out.println(displayUsername + " assigned with ID: " + assignedID + "\n");
            clients.put(assignedID, this);

            //tell client their ID
            Message idMsg = new Message("SERVER", assignedID);
            out.writeObject(idMsg);
            out.flush();

            //tell client who coordinator is
            Message CoordMsg = new Message("SERVER", "COORDINATOR:" + Server.idCoordinator);
            out.writeObject(CoordMsg);
            out.flush();

            //implementing loop
            boolean a = true;
            while (a){
                Message message = (Message)in.readObject();
                System.out.println(message);

                // graceful leave
                if (message.content.equals("LEAVE")) {
                    handleLeave(displayUsername);
                    return;
                }

                //pong reply clears the ping flag
                if (message.content.equals("PONG")) {
                    awaitingPong.set(false);
                    continue;
                }

                //private messaging
                if (message.content.startsWith("PRIVATE:")){
                    String[] parts = message.content.split(":",3);
                    String targetID = parts[1].trim();
                    String PrivateMessage = parts[2];

                    ClientHandler target = clients.get(targetID);
                    if (target == null){
                        this.sendMessage(new Message("SERVER", "Invalid target ID: " + targetID));
                        continue;
                    }
                    target.sendMessage(new Message(message.Username, "Private Message:" + PrivateMessage));
                    continue;
                }

                //Broadcast messaging
                for (ClientHandler client : clients.values()){
                    client.sendMessage(message);
                }
            }

        //socket drop — treat as failure
        } catch (SocketException e){
            System.out.println(assignedID + " socket error: " + e.getMessage());
            handleFailure(displayUsername);

        } catch (IOException | ClassNotFoundException e) {
            handleFailure(displayUsername);
        }
    }

    //Requirement 1: graceful leave
    private void handleLeave(String displayUsername) {
        synchronized (clients) {
            if (!clients.containsKey(assignedID)) return;
            clients.remove(assignedID);
        }
        try { socket.close(); } catch (IOException ignored) {}
        System.out.println(assignedID + " (" + displayUsername + ") left gracefully.");
        broadcastToAllStatic(new Message("SERVER", "MEMBER_LEFT:" + assignedID + ":" + displayUsername));
        if (assignedID.equals(Server.idCoordinator)) {
            electCoordinator();
        }
    }

    //failure (missed ping or socket error)
    private void handleFailure(String displayUsername) {
        synchronized (clients) {
            if (!clients.containsKey(assignedID)) return;
            clients.remove(assignedID);
        }
        try { socket.close(); } catch (IOException ignored) {}
        System.out.println(assignedID + " removed due to failure.");
        broadcastToAllStatic(new Message("SERVER", "MEMBER_LEFT:" + assignedID + ":" + displayUsername));
        if (assignedID.equals(Server.idCoordinator)) {
            electCoordinator();
        }
    }

    // elect lowest ID as new coordinator
    static void electCoordinator() {
        synchronized (Server.clients) {
            if (Server.clients.isEmpty()) {
                Server.idCoordinator = null;
                System.out.println("[Election] No members left.");
                return;
            }
            String newCoord = Server.clients.keySet().stream()
                .min(Comparator.comparingInt(id -> Integer.parseInt(id.substring(1))))
                .orElse(null);
            Server.idCoordinator = newCoord;
            System.out.println("[Election] New coordinator: " + newCoord);
            broadcastToAllStatic(new Message("SERVER", "COORDINATOR:" + newCoord));
        }
    }

    // ping loop started once from Server.main()
    static void startPingLoop() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            List<ClientHandler> snapshot;
            synchronized (Server.clients) {
                snapshot = new ArrayList<>(Server.clients.values());
            }
            for (ClientHandler c : snapshot) {
                c.awaitingPong.set(true);
                c.sendMessage(new Message("SERVER", "PING"));
            }
            try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
            for (ClientHandler c : snapshot) {
                if (c.awaitingPong.get()) {
                    System.out.println("[Ping] " + c.assignedID + " missed PONG — removing.");
                    c.handleFailure("");
                }
            }
        }, 5000, 5000, TimeUnit.MILLISECONDS);
        System.out.println("[Ping] Loop started (5s interval, 3s timeout).");
    }

    static void broadcastToAllStatic(Message msg) {
        synchronized (Server.clients) {
            for (ClientHandler c : Server.clients.values()) {
                c.sendMessage(msg);
            }
        }
    }
}