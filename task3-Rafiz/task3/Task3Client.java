package task3;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class Task3Client {
    private Socket socket;
    private BufferedReader input;
    private DataOutputStream out;

    public Task3Client(String address, int port) {
        try {
            socket = new Socket(address, port);
            System.out.println("Connected");

            input = new BufferedReader(new InputStreamReader(System.in));
            out = new DataOutputStream(socket.getOutputStream());
        } catch (UnknownHostException e) {
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(e);
        }

        String line = "";

        while (!line.equals("Stop")) {
            try {
                line = input.readLine();
                out.writeUTF(line);
            } catch (IOException e) {
                System.out.println(e);
            }
        }

        try {
            input.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public static void main(String[] args) {
        new Task3Client("127.0.0.1", 6666);
    }
}