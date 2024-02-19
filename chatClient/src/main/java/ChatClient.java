import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private String clientName;

    public ChatClient(String serverAddress, int serverPort) {
        try {
            socket = new Socket(serverAddress, serverPort);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.print("Enter your name ");
            Scanner scanner = new Scanner(System.in);
            clientName = scanner.nextLine();

            writer.println(clientName);

            Thread serverThread = new Thread(new ServerHandler());
            serverThread.start();

            System.out.println(
                    "Connected to the server.");
            System.out.println(
                    "To send a private message, enter: /whisper message recipient");

            String message;
            while (true) {
                message = scanner.nextLine();

                if (message.equals("/exit")) {
                    break;
                }

                writer.println(message);
            }

            writer.close();
            reader.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ServerHandler implements Runnable {
        @Override
        public void run() {
            String message;
            try {
                while ((message = reader.readLine()) != null) {
                    System.out.println(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        String serverAddress = "localhost";
        int serverPort = 666;

        new ChatClient(serverAddress, serverPort);
    }
}