import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {

    private final String host;
    private final int port;
    private Socket socket;
    private InputStream input;
    private OutputStream output;
    private ClientGui gui;
    private String username;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void setGui(ClientGui gui) {
        this.gui = gui;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean connect() {
        try {
            socket = new Socket(host, port);
            input = socket.getInputStream();
            output = socket.getOutputStream();

            System.out.println("=".repeat(70));
            System.out.println("Connected to " + host + ":" + port);
            System.out.println("=".repeat(70));
            System.out.println("\nThis is an EXAMPLE client, NOT the chat protocol!");
            System.out.println("\nAvailable commands:");
            System.out.println("  name <new name>  - Change username");
            System.out.println("  msg <text>    - send message");
            System.out.println("  read          - Get last 20 messages");
            System.out.println("  time          - Get server time");
            System.out.println("  quit          - Disconnect");
            System.out.println("=".repeat(70));
            return true;

        } catch (IOException e) {
            System.err.println("Connection failed: " + e.getMessage());
            return false;
        }
    }

    public void send(String key, String valueStr) {
        if (output == null) {
            System.err.println("! Error sending : not connected (output is null)");
            return;
        }
        try {
            byte[] message = KLVExample.encodeKLV(key,
                    valueStr.getBytes(StandardCharsets.UTF_8));

            System.out.println("\n→ Sending: " + key + ":" + valueStr.length() + ":" + valueStr);
            output.write(message);
            System.out.println("Hex sent: " + Client.bytesToHex(message));
            output.flush();

        } catch (Exception e) {
            System.err.println("! Error sending: " + e.getMessage());
        }
    }

    private KLVMessage readKLVFromSocket() throws IOException {
        byte[] keyBytes = recvExact(4);
        if (keyBytes == null) return null;

        byte[] lengthBytes = recvExact(4);
        if (lengthBytes == null) return null;

        ByteBuffer lengthBuffer = ByteBuffer.wrap(lengthBytes);
        int length = lengthBuffer.getInt();

        byte[] valueBytes = recvExact(length);
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

    private byte[] recvExact(int numBytes) throws IOException {
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

    public void chatRoom() {
        Scanner scanner = new Scanner(System.in);

        Thread listener = new Thread(() -> {
            try {
                System.out.println("[Listener] Thread started, waiting for messages...");
                while (socket != null && !socket.isClosed()) {
                    KLVMessage message = readKLVFromSocket();
                    if (message == null) {
                        System.out.println("[Listener] Connection closed, exiting listener thread");
                        if (gui != null) {
                            gui.closeWindow();
                        }
                        break;
                    }

                    String respText = new String(message.value, StandardCharsets.UTF_8);
                    System.out.println("\n← Received: " + message.key + ":" +
                            message.value.length + ":" + respText);

                    // Handle RESP messages (status codes)
                    if (message.key.equals("RESP")) {
                        String statusCode = respText.trim();

                        if ("200".equals(statusCode)) {
                            System.out.println("[Status Success (200)");
                        } else if ("400".equals(statusCode)) {
                            System.err.println("[Status] Bad Request (400) - Message failed");
                        } else if ("403".equals(statusCode)) {
                            System.err.println("[Status] Forbidden (403) - Operation not allowed");
                        } else {
                            System.out.println("[Status] Code: " + statusCode);
                        }
                        System.out.print("> ");
                        continue;
                    }

                    if (message.key.equals("PFP")) {
                        if (gui != null) {
                            String[] parts = respText.split(":", 2);
                            if (parts.length == 2) {
                                String user = parts[0];
                                try {
                                    int indx = Integer.parseInt(parts[1]);
                                    gui.setUserPfp(user, indx);
                                } catch (NumberFormatException ignored) {
                                }
                            }
                        }
                        System.out.print("> ");
                        continue;
                    }

                    if (gui != null) {
                        if (message.key.equals("NAME")) {
                            String[] parts = respText.split(" has changed their name to ");
                            if (parts.length == 2) {
                                String oldName = parts[0];
                                String newName = parts[1];
                                gui.updateUserName(oldName, newName);
                            }
                        }

                        if (message.key.equals("READ")) {
                            String[] historyMessages = respText.split("\n");
                            for (String msg : historyMessages) {
                                if (!msg.trim().isEmpty()) {
                                    gui.appendHistoryMessage(msg);
                                }
                            }
                        } else {
                            gui.appendMessage(respText);
                        }
                    }

                    System.out.print("> ");
                }
            } catch (Exception e) {
                System.err.println("\n! Listening error: " + e.getMessage());
                e.printStackTrace();
            }
        });
        listener.setDaemon(true);
        listener.start();

        if (username == null)
            send("JOIN", "");
        else
            send("JOIN", username);

        while (true) {
            try {
                System.out.print("\n> ");
                String userInput = scanner.nextLine().trim();

                if (userInput.isEmpty()) {
                    continue;
                }

                String[] parts = userInput.split("\\s+", 2);
                String command = parts[0].toLowerCase();

                switch (command) {
                    case "quit":
                        System.out.println("\nSending QUIT command...");
                        send("QUIT", "");
                        System.out.println("Disconnecting...");
                        if (gui != null) {
                            gui.closeWindow();
                        }
                        return;

                    case "name":
                        if (parts.length < 2) {
                            System.out.println("Usage: name <new name>");
                            continue;
                        }
                        send("NAME", parts[1]);
                        username = parts[1];
                        break;

                    case "msg":
                        if (parts.length < 2) {
                            System.out.println("Usage: msg <text>");
                            continue;
                        }
                        send("MSG", parts[1]);
                        break;

                    case "time":
                        send("TIME", "");
                        break;
                    case "read":
                        send("READ", "");
                        break;
                    default:
                        System.out.println("Unknown command: " + command);
                        System.out.println("Available: name, msg, time, read, quit");
                }

            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                break;
            }
        }

        scanner.close();
    }

    public void close() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
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

    static class KLVResponse {
        String key;
        String text;

        KLVResponse(String key, String text) {
            this.key = key;
            this.text = text;
        }
    }

    public static void main(String[] args) {
        String host = "localhost";
        int port = 9001;

        if (args.length > 0) {
            host = args[0];
        }

        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port: " + args[1]);
                System.exit(1);
            }
        }

        Client client = new Client(host, port);

        if (args.length > 2) {
            client.setUsername(args[2]);
        }

        ClientGui gui = new ClientGui(host, port);
        client.setGui(gui);
        gui.setClient(client);

        if (!client.connect()) {
            System.exit(1);
        }

        try {
            client.chatRoom();
        } finally {
            client.close();
        }

        System.out.println("\nDisconnected.");
    }
}


