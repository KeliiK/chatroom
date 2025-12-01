import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * Java KLV Example Client - Educational Demo
 *
 * This client connects to JavaExampleServer and demonstrates:
 * - Connecting to a server
 * - Sending KLV messages
 * - Receiving and parsing responses
 * - Interactive user input
 *
 * This is NOT the chat protocol - it's a simpler example to learn from.
 * Study this code and apply the patterns to your chat client!
 */
public class JavaExampleClient {

    private final String host;
    private final int port;
    private Socket socket;
    private InputStream input;
    private OutputStream output;

    public JavaExampleClient(String host, int port) {
        this.host = host;
        this.port = port;
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
            System.out.println("  helo <name>  - Get a heloing from server");
            System.out.println("  echo <text>   - Echo text back");
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
        try {
            // Encode and send
            byte[] message = KLVExample.encodeKLV(key,
                valueStr.getBytes(StandardCharsets.UTF_8));

            System.out.println("\n→ Sending: " + key + ":" + valueStr.length() + ":" + valueStr);
            output.write(message);
            System.out.println("Hex sent: " + JavaExampleClient.bytesToHex(message));
            output.flush();

        } catch (Exception e) {
            System.err.println("! Error sending: " + e.getMessage());
        }
    }

    private KLVMessage readKLVFromSocket() throws IOException {
        // Read 4 bytes for key
        byte[] keyBytes = recvExact(4);
        if (keyBytes == null) return null;

        // Read 4 bytes for length
        byte[] lengthBytes = recvExact(4);
        if (lengthBytes == null) return null;

        ByteBuffer lengthBuffer = ByteBuffer.wrap(lengthBytes);
        int length = lengthBuffer.getInt();

        // Read exact value bytes
        byte[] valueBytes = recvExact(length);
        if (valueBytes == null) return null;

        // Parse key (remove null padding)
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

    /**
     * Receive exactly numBytes from input stream.
     *
     * CRITICAL: InputStream.read() may return fewer bytes than requested!
     * You MUST loop until you have all bytes.
     */
    private byte[] recvExact(int numBytes) throws IOException {
        byte[] data = new byte[numBytes];
        int totalRead = 0;

        while (totalRead < numBytes) {
            int bytesRead = input.read(data, totalRead, numBytes - totalRead);
            if (bytesRead == -1) {
                return null; // Connection closed
            }
            totalRead += bytesRead;
        }

        return data;
    }

    public void interactiveMode() {
        Scanner scanner = new Scanner(System.in);

        // Start listening thread to continuously receive messages from server
        Thread listener = new Thread(() -> {
            try {
                System.out.println("[Listener] Thread started, waiting for messages...");
                while (socket != null && !socket.isClosed()) {
                    KLVMessage message = readKLVFromSocket();
                    if (message == null) {
                        // Connection closed
                        System.out.println("[Listener] Connection closed, exiting listener thread");
                        break;
                    }
                    
                    String respText = new String(message.value, StandardCharsets.UTF_8);
                    System.out.println("\n← Received: " + message.key + ":" +
                        message.value.length + ":" + respText);
                    System.out.print("> "); // Re-print prompt after receiving message
                }
            } catch (Exception e) {
                // Print error details for debugging
                System.err.println("\n! Listening error: " + e.getMessage());
                e.printStackTrace();
            }
        });
        listener.setDaemon(true);
        listener.start();

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
                        return;

                    case "helo":
                        if (parts.length < 2) {
                            System.out.println("Usage: helo <name>");
                            continue;
                        }
                        send("HELO", parts[1]);
                        break;

                    case "echo":
                        if (parts.length < 2) {
                            System.out.println("Usage: echo <text>");
                            continue;
                        }
                        send("ECHO", parts[1]);
                        break;

                    case "time":
                        send("TIME", "");
                        break;

                    default:
                        System.out.println("Unknown command: " + command);
                        System.out.println("Available: helo, echo, time, quit");
                }

            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                break;
            }
        }

        scanner.close();
    }

    public void demoMode() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("DEMO MODE - Sending Example Commands");
        System.out.println("=".repeat(70));

        // Start listening thread for demo mode too
        Thread listener = new Thread(() -> {
            try {
                while (socket != null && !socket.isClosed()) {
                    KLVMessage message = readKLVFromSocket();
                    if (message == null) {
                        break;
                    }
                    String respText = new String(message.value, StandardCharsets.UTF_8);
                    System.out.println("\n← Received: " + message.key + ":" +
                        message.value.length + ":" + respText);
                }
            } catch (Exception e) {
                // Only print error if socket is still open (connection was closed intentionally)
                if (socket != null && !socket.isClosed()) {
                    System.err.println("\n! Listening error: " + e.getMessage());
                }
            }
        });
        listener.setDaemon(true);
        listener.start();

        try {
            // Test HELO
            System.out.println("\n[1] Testing HELO command:");
            send("HELO", "Alice");
            Thread.sleep(500); // Give time for response

            // Test ECHO
            System.out.println("\n[2] Testing ECHO command:");
            send("ECHO", "Hello, World!");
            Thread.sleep(500);

            // Test TIME
            System.out.println("\n[3] Testing TIME command:");
            send("TIME", "");
            Thread.sleep(500);

            // Test unknown command
            System.out.println("\n[4] Testing unknown command (error handling):");
            send("UNKN", "test");
            Thread.sleep(500);

            // QUIT
            System.out.println("\n[5] Sending QUIT:");
            send("QUIT", "");
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("\n" + "=".repeat(70));
        System.out.println("Demo complete!");
        System.out.println("=".repeat(70));
    }

    public void close() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            // Ignore
        }
    }

    /**
     * Convert byte array to hex string with spaces between bytes.
     * Example: [0x48, 0x45, 0x4C, 0x4F] -> "48 45 4c 4f"
     */
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

    /**
     * Simple KLV message structure
     */
    static class KLVMessage {
        String key;
        byte[] value;

        KLVMessage(String key, byte[] value) {
            this.key = key;
            this.value = value;
        }
    }

    /**
     * Represents a parsed response
     */
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
        String mode = "interactive";

        // Parse arguments
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
        if (args.length > 2) {
            mode = args[2];
        }

        // Create and connect client
        JavaExampleClient client = new JavaExampleClient(host, port);

        if (!client.connect()) {
            System.exit(1);
        }

        try {
            if (mode.equals("demo")) {
                client.demoMode();
            } else {
                client.interactiveMode();
            }
        } finally {
            client.close();
        }

        System.out.println("\nDisconnected.");
    }
}
