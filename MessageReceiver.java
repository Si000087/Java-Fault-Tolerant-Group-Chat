//client side
import java.io.*;
import java.net.*;

public class MessageReceiver implements Runnable {
    private Socket socket;
    private ObjectOutputStream out; // from main: needed to reply PONG

    public MessageReceiver(Socket socket, ObjectOutputStream out) {
        this.socket = socket;
        this.out = out;
    }

    @Override
    public void run() {
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(socket.getInputStream());
            while (true) {
                Message message = (Message) in.readObject();

                // from shani: handle /list member entries before SERVER check
                if (message.content.startsWith("MEMBER:")) {
                    String[] parts = message.content.split(":");
                    String id   = parts[1];
                    String name = parts[2];
                    String ip   = parts[3];
                    String port = parts[4];
                    System.out.println("Member ID: " + id + ", username: " + name
                        + ", IP: " + ip + ", port: " + port);
                    continue;
                }

                if ("SERVER".equals(message.Username)) {

                    if (message.content.startsWith("Invalid")) {
                        System.out.println(message.content);
                        continue;
                    }

                    if (message.content.startsWith("COORDINATOR:")) {
                        String coord = message.content.substring("COORDINATOR:".length());
                        System.out.println("The current coordinator is: " + coord);
                        continue;
                    }

                    // from main: member left notification
                    if (message.content.startsWith("MEMBER_LEFT:")) {
                        String[] parts = message.content.split(":", 3);
                        String leftID   = parts[1];
                        String leftName = parts[2];
                        System.out.println("<-- " + leftName + " (" + leftID + ") has left.");
                        continue;
                    }

                    // from main: reply PONG to keep connection alive
                    if (message.content.equals("PING")) {
                        out.writeObject(new Message("CLIENT", "PONG"));
                        out.flush();
                        continue;
                    }

                    // default: ID assignment message
                    System.out.println("Your unique ID: " + message.content);
                    continue;
                }

                // private message display
                if (message.content.startsWith("Private Message:")) {
                    System.out.println("[PRIVATE] " + message.Username + ": "
                        + message.content.substring("Private Message:".length()));
                    continue;
                }

                // normal broadcast message
                System.out.println(message);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Disconnected from the Server.");
        } finally {
            try { if (in != null) in.close(); } catch (IOException ignored) {}
        }
    }
}
