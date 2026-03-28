import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static void sendToServer(ObjectOutputStream out, Message message) throws IOException {
        synchronized (out) {
            out.writeObject(message);
            out.flush();
        }
    }

    public static void main(String[] args) {
        Socket socket = null;
        ObjectOutputStream out = null;
        Thread receiverThread = null;
        Scanner scanner = new Scanner(System.in);
        try {
            String host = args.length > 0 && !args[0].isBlank() ? args[0] : "127.0.0.1";
            socket = new Socket(host, 12345);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();

            System.out.println("Enter your name:\n");
            String username = scanner.nextLine();
            System.out.println("Connected to Server");
            System.out.println("Welcome: " + username);
            sendToServer(out, new Message(username, "JOIN"));

            MessageReceiver receiver = new MessageReceiver(socket, out);
            receiverThread = new Thread(receiver);
            receiverThread.start();

            while (true) {
                String myMessage = scanner.nextLine();

                if (myMessage.equalsIgnoreCase("/list")) {
                    sendToServer(out, new Message(username, "/list"));
                    continue;
                }

                if (myMessage.equalsIgnoreCase("/private")) {
                    System.out.println("Enter target username or ID: ");
                    String targetUID = scanner.nextLine();
                    System.out.println("Enter message:");
                    String privateMessage = scanner.nextLine();
                    String encoded = "PRIVATE:" + targetUID + ": " + privateMessage;
                    sendToServer(out, new Message(username, encoded));
                    System.out.println("Message sent.");
                    continue;
                }

                if (myMessage.equalsIgnoreCase("quit") || myMessage.equalsIgnoreCase("leave")) {
                    sendToServer(out, new Message(username, "LEAVE"));
                    System.out.println("Disconnecting");
                    socket.shutdownOutput();
                    break;
                }

                sendToServer(out, new Message(username, myMessage));
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Error: " + e.getMessage());
                Thread.currentThread().interrupt();
            }

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            if (receiverThread != null) {
                try {
                    receiverThread.join(1500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            try { if (out != null) out.close(); } catch (IOException ignored) {}
            try { if (socket != null && !socket.isClosed()) socket.close(); } catch (IOException ignored) {}
            scanner.close();
        }
    }
}
