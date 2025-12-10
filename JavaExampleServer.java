import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class JavaExampleServer {

    private final String host;
    private final int port;
    private ServerSocket serverSocket;
    private volatile boolean running;
    private final AtomicInteger clientCount = new AtomicInteger(0);
    private static final List<OutputStream> outputStreamList = new ArrayList<>();
    
    private static final Queue<String> messageHistory = new ArrayDeque<>();
    private static final int MAX_HISTORY_SIZE = 20;
    private static final Object historyLock = new Object();
    private static int numOfClients = 0;

    public JavaExampleServer(String host, int port) {
        this.host = host;
        this.port = port;
        this.running = false;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;

        System.out.println("=".repeat(70));
        System.out.println("Java KLV Example Server");
        System.out.println("=".repeat(70));
        System.out.println("Listening on " + host + ":" + port);
        System.out.println("\nThis is an EXAMPLE server, NOT the chat protocol!");
        System.out.println("\nSupported commands:");
        System.out.println("  NAME:length:name   - send back new username  ");
        System.out.println("  MSG:length:text    - Send back text");
        System.out.println("  READ:length:text   - send back messages");
        System.out.println("  TIME:0:            - Get server time");
        System.out.println("  QUIT:0:            - Disconnect");
        System.out.println("\nPress Ctrl+C to stop.");
        System.out.println("=".repeat(70));

        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                int clientId = clientCount.incrementAndGet();

                InetSocketAddress address = (InetSocketAddress) clientSocket.getRemoteSocketAddress();
                System.out.println("\n[Client " + clientId + "] Connected from " + address);

                numOfClients++;
                Thread clientThread = new Thread(new ClientHandler(clientSocket, clientId));
                clientThread.setDaemon(true);
                clientThread.start();


            } catch (IOException e) {
                if (running) {
                    System.err.println("Error accepting connection: " + e.getMessage());
                }
            }
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server: " + e.getMessage());
        }
        System.out.println("Server stopped.");
    }

    private static void addToHistory(String message) {
        synchronized (historyLock) {
            messageHistory.add(message);
            if (messageHistory.size() > MAX_HISTORY_SIZE) {
                messageHistory.remove();
            }
        }
    }
    
    private static List<String> getHistory() {
        synchronized (historyLock) {
            return new ArrayList<>(messageHistory);
        }
    }
    
    private class ClientHandler implements Runnable {
        private final Socket socket;
        private final int clientId;
        private String username = "user" + numOfClients;

        public ClientHandler(Socket socket, int clientId) {
            this.socket = socket;
            this.clientId = clientId;
        }

        @Override
        public void run() {
            OutputStream output = null;
            try {
                InputStream input = socket.getInputStream();
                output = socket.getOutputStream();
                synchronized (outputStreamList) {
                    outputStreamList.add(output);
                }

                while (running && !socket.isClosed()) {
                    KLVMessage message = readKLVFromSocket(input);
                    if (message == null) {
                        break;
                    }

                    String valueStr = new String(message.value, StandardCharsets.UTF_8);
                    
                    System.out.println("[Client " + clientId + "] Received: " +
                        message.key + ":" + message.value.length + ":" + valueStr);

                    byte[] response = processCommand(message.key, message.value);
                    if (response == null) {
                        break;
                    }

                    System.out.println("[DEBUG] Raw bytes sent: " + JavaExampleServer.bytesToHex(response));

                    if (message.key.equals("MSG")) {
                        KLVExample.KLVMessage respMsg = KLVExample.decodeKLV(response);
                        String responseValue = new String(respMsg.value, StandardCharsets.UTF_8);
                        addToHistory(responseValue);
                    }

                    if (message.key.equals("READ")) {
                        output.write(response);
                        output.flush();
                    } else {
                        broadCastResponse(response);
                    }

                    if (message.key.equals("QUIT")) {
                        break;
                    }
                }

            } catch (Exception e) {
                System.err.println("[Client " + clientId + "] Error: " + e.getMessage());
            } finally {
                if (output != null) {
                    synchronized (outputStreamList) {
                        outputStreamList.remove(output);
                    }
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
                System.out.println("[Client " + clientId + "] Disconnected");
            }
        }

        private byte[] processCommand(String key, byte[] value) throws Exception {
            String name = null;
            switch (key) {
                case "JOIN":
                    name = new String(value,  StandardCharsets.UTF_8);
                    if (name.length() != 0)
                        username = name;
                    String joinMsg = username + " joined";
                    return KLVExample.encodeKLV("JOIN", joinMsg.getBytes(StandardCharsets.UTF_8));
                case "NAME":
                    name = new String(value, StandardCharsets.UTF_8);
                    String greeting = username + " has changed their name to " + name;
                    username = name;
                    return KLVExample.encodeKLV("NAME", greeting.getBytes(StandardCharsets.UTF_8));

                case "MSG":
                    String valueStr = new String(value, StandardCharsets.UTF_8);
                    valueStr = username + ":\t" + valueStr;
                    return KLVExample.encodeKLV("MSG", valueStr.getBytes(StandardCharsets.UTF_8));

                case "TIME":
                    String timestamp = LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    return KLVExample.encodeKLV("TIME", timestamp.getBytes(StandardCharsets.UTF_8));
                    
                case "READ":
                    List<String> history = getHistory();
                    String historyText;
                    if (history.isEmpty()) {
                        historyText = "No message history available.";
                    } else {
                        historyText = String.join("\n", history);
                    }
                    return KLVExample.encodeKLV("READ", historyText.getBytes(StandardCharsets.UTF_8));
                    
                case "QUIT":
                    String leaving = username + " has left :(";
                    return KLVExample.encodeKLV("QUIT", leaving.getBytes(StandardCharsets.UTF_8));

                default:
                    String error = "Unknown command: " + key;
                    return KLVExample.encodeKLV("ERR", error.getBytes(StandardCharsets.UTF_8));
            }
        }

        private KLVMessage readKLVFromSocket(InputStream input) throws IOException {
            byte[] keyBytes = recvExact(input, 4);
            if (keyBytes == null) return null;

            byte[] lengthBytes = recvExact(input, 4);
            if (lengthBytes == null) return null;

            ByteBuffer lengthBuffer = ByteBuffer.wrap(lengthBytes);
            int length = lengthBuffer.getInt();

            byte[] valueBytes = recvExact(input, length);
            if (valueBytes == null) return null;

            int keyLength = 4;
            for (int i = 0; i < 4; i++) {
                if (keyBytes[i] == 0) {
                    keyLength = i;
                    break;
                }
            }
            String key = new String(keyBytes, 0, keyLength, StandardCharsets.US_ASCII);

            return new KLVMessage(key, valueBytes);
        }

        private byte[] recvExact(InputStream input, int numBytes) throws IOException {
            byte[] data = new byte[numBytes];
            int totalRead = 0;

            while (totalRead < numBytes) {
                int bytesRead = input.read(data, totalRead, numBytes - totalRead);
                if (bytesRead == -1) {
                    return null;
                }
                totalRead += bytesRead;
            }


            return data;
        }
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(String.format("%02x", bytes[i]));
        }
        return sb.toString();
    }

    static class KLVMessage {
        String key;
        byte[] value;

        KLVMessage(String key, byte[] value) {
            this.key = key;
            this.value = value;
        }
    }


    public static void broadCastResponse(byte[] response) {
        synchronized (outputStreamList) {
            System.out.println("[Broadcast] Sending to " + outputStreamList.size() + " client(s)");
            outputStreamList.removeIf(output -> {
                try {
                    output.write(response);
                    output.flush();
                    System.out.println("[Broadcast] Successfully sent to one client");
                    return false;
                } catch (Exception e) {
                    System.err.println("[Broadcast] Failed to send to client: " + e.getMessage());
                    return true;
                }
            });
            System.out.println("[Broadcast] Remaining clients: " + outputStreamList.size());
        }
    }

    public static void main(String[] args) {
        int port = 9001;

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port: " + args[0]);
                System.exit(1);
            }
        }

        JavaExampleServer server = new JavaExampleServer("0.0.0.0", port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n\nShutting down server...");
            server.stop();
        }));

        try {
            server.start();
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            System.exit(1);
        }
    }
}
