// basic client socket
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        Socket socket = null;
        ObjectOutputStream out = null;
        Scanner scanner = new Scanner(System.in);
        try {
            socket = new Socket("192.168.0.249", 12345);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();

            System.out.println("Enter your name:\n");
            String username = scanner.nextLine();
            System.out.println("Connected to Server");
            System.out.println("Welcome: " + username);
            out.writeObject(new Message(username, "JOIN"));
            out.flush();

            // from main: pass out so MessageReceiver can reply to PING
            MessageReceiver receiver = new MessageReceiver(socket, out);
            new Thread(receiver).start();

            while (true) {
                String myMessage = scanner.nextLine();

                // from shani: /list command
                if (myMessage.equalsIgnoreCase("/list")) {
                    out.writeObject(new Message(username, "/list"));
                    out.flush();
                    continue;
                }

                // private messaging
                if (myMessage.equalsIgnoreCase("/private")) {
                    System.out.println("Enter target ID: ");
                    String targetUID = scanner.nextLine();
                    System.out.println("Enter message:");
                    String privateMessage = scanner.nextLine();
                    String encoded = "PRIVATE:" + targetUID + ": " + privateMessage;
                    out.writeObject(new Message(username, encoded));
                    out.flush();
                    System.out.println("Message sent.");
                    continue;
                }

                if (myMessage.equals("quit")) {
                    out.writeObject(new Message(username, "LEAVE"));
                    out.flush();
                    System.out.println("Disconnecting");
                    break;
                }

                out.writeObject(new Message(username, myMessage));
                out.flush();
            }

            try { Thread.sleep(1000); } catch (InterruptedException e) {
                System.out.println("Error: " + e.getMessage());
            }

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            // from main: proper cleanup
            try { if (out != null) out.close(); } catch (IOException ignored) {}
            try { if (socket != null && !socket.isClosed()) socket.close(); } catch (IOException ignored) {}
            scanner.close();
        }
    }
}
