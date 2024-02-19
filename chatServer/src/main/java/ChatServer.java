import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private Socket socket;
    private ServerSocket serverSocket;
    private List<Socket> clients;
    private Map<Socket, String> clientNames;

    public ChatServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            clients = new ArrayList<>();
            clientNames = new HashMap<>();

            System.out.println("the server is running on the port " + port);

            while (true) {
                socket = serverSocket.accept();
                clients.add(socket);

                Thread clientThread = new Thread(new ClientHandler(socket));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter writer;
        private BufferedReader reader;
        private String clientName;

        public ClientHandler(Socket socket) {
            try {
                this.clientSocket = socket;
                writer = new PrintWriter(clientSocket.getOutputStream(), true);
                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                clientName = reader.readLine();
                clientNames.put(clientSocket, clientName);

                System.out.println(
                        "Client connected:" + clientName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            String message;
            try {
                while ((message = reader.readLine()) != null) {
                    if (message.startsWith("/whisper")) {
                        String[] parts = message.split(" ");
                        String recipient = parts[1];
                        String privateMessage = parts[2];

                        sendPrivateMessage(clientNames.get(clientSocket), recipient, privateMessage);
                    } else {
                        sendToAll(clientName + ": " + message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void sendToAll(String message) {
            for (Socket client : clients) {
                try {
                    PrintWriter clientWriter = new PrintWriter(client.getOutputStream(), true);
                    clientWriter.println(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void sendPrivateMessage(String sender, String recipient, String message) {
            String privateMessage = "[Private from " + sender + "]: " + message;

            for (Socket client : clients) {
                if (clientNames.get(client).equals(recipient)) {
                    try {
                        PrintWriter clientWriter = new PrintWriter(client.getOutputStream(), true);
                        clientWriter.println(privateMessage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        int port = 666;
        new ChatServer(port);
    }
}