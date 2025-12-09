import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * KLV Protocol Examples - Java
 * Educational examples demonstrating Key-Length-Value encoding/decoding
 *
 * IMPORTANT: This uses GENERIC examples (USER, DATA, INFO) that are NOT
 * part of your chat protocol. You must apply these concepts to implement
 * JOIN, MSG, READ, EXIT yourself!
 */
public class KLVExample {

    /**
     * Represents a decoded KLV structure
     */
    static class KLVMessage {
        String key;
        byte[] value;

        KLVMessage(String key, byte[] value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return String.format("%s:%d:%s", key, value.length,
                    new String(value, StandardCharsets.UTF_8));
        }
    }

    /**
     * Encode a KLV structure into binary format.
     *
     * @param key   4-character ASCII key (will be padded with null bytes if shorter)
     * @param value The value as bytes
     * @return Complete KLV structure as bytes
     */
    public static byte[] encodeKLV(String key, byte[] value) throws Exception {
        // Ensure key is exactly 4 bytes, pad with null bytes if needed
        byte[] keyBytes = key.getBytes(StandardCharsets.US_ASCII);
        if (keyBytes.length > 4) {
            throw new IllegalArgumentException("Key '" + key + "' is too long (max 4 bytes)");
        }

        byte[] paddedKey = new byte[4];
        System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
        // Remaining bytes are already 0 (null bytes)

        // Encode length as 4-byte big-endian integer
        ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
        lengthBuffer.putInt(value.length);
        byte[] lengthBytes = lengthBuffer.array();

        // Concatenate: Key + Length + Value
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(paddedKey);
        output.write(lengthBytes);
        output.write(value);

        return output.toByteArray();
    }

    /**
     * Decode a KLV structure from binary format.
     *
     * @param data Binary data starting with a KLV structure
     * @return Decoded KLV message
     */
    public static KLVMessage decodeKLV(byte[] data) throws Exception {
        return decodeKLV(data, 0);
    }

    /**
     * Decode a KLV structure starting at a specific offset.
     *
     * @param data   Binary data containing a KLV structure
     * @param offset Starting position in the data array
     * @return Decoded KLV message
     */
    public static KLVMessage decodeKLV(byte[] data, int offset) throws Exception {
        if (data.length - offset < 8) {
            throw new IllegalArgumentException(
                    "Data too short for KLV structure (need at least 8 bytes)");
        }

        // Read key (4 bytes)
        byte[] keyBytes = Arrays.copyOfRange(data, offset, offset + 4);
        // Remove null padding and convert to string
        int keyLength = 4;
        for (int i = 0; i < 4; i++) {
            if (keyBytes[i] == 0) {
                keyLength = i;
                break;
            }
        }
        String key = new String(keyBytes, 0, keyLength, StandardCharsets.US_ASCII);

        // Read length (4 bytes, big-endian)
        ByteBuffer lengthBuffer = ByteBuffer.wrap(data, offset + 4, 4);
        int length = lengthBuffer.getInt();

        // Read value
        if (data.length - offset < 8 + length) {
            throw new IllegalArgumentException(
                    String.format("Data too short: expected %d bytes, got %d",
                            8 + length, data.length - offset));
        }
        byte[] value = Arrays.copyOfRange(data, offset + 8, offset + 8 + length);

        return new KLVMessage(key, value);
    }

    /**
     * Encode a KLV structure containing nested KLV items.
     *
     * @param key         Outer key
     * @param nestedItems List of KLV messages to nest inside
     * @return Complete nested KLV structure
     */
    public static byte[] encodeNestedKLV(String key, List<KLVMessage> nestedItems)
            throws Exception {
        ByteArrayOutputStream nestedData = new ByteArrayOutputStream();

        // Encode each nested item
        for (KLVMessage item : nestedItems) {
            byte[] itemBytes = encodeKLV(item.key, item.value);
            nestedData.write(itemBytes);
        }

        // Wrap in outer KLV
        return encodeKLV(key, nestedData.toByteArray());
    }

    /**
     * Decode a KLV structure that contains nested KLV items.
     *
     * @param data Binary data containing nested KLV structure
     * @return List of nested KLV messages
     */
    public static List<KLVMessage> decodeNestedKLV(byte[] data) throws Exception {
        // Decode outer structure
        KLVMessage outer = decodeKLV(data);

        // Parse nested items from the value
        List<KLVMessage> nestedItems = new ArrayList<>();
        int offset = 0;
        byte[] innerData = outer.value;

        while (offset + 8 <= innerData.length) {
            try {
                KLVMessage nested = decodeKLV(innerData, offset);
                nestedItems.add(nested);
                offset += 8 + nested.value.length;
            } catch (Exception e) {
                break; // No more complete KLV structures
            }
        }

        return nestedItems;
    }

    /**
     * Pretty print binary data in hex format for debugging.
     */
    public static void hexDump(byte[] data, String label) {
        if (!label.isEmpty()) {
            System.out.println("\n" + label + ":");
        }

        // Print hex
        System.out.print("  Hex: ");
        for (int i = 0; i < data.length; i++) {
            System.out.printf("%02x ", data[i]);
        }
        System.out.println();

        System.out.println("  Length: " + data.length + " bytes");

        // Print ASCII representation
        System.out.print("  ASCII: ");
        for (byte b : data) {
            char c = (char) (b & 0xFF);
            System.out.print((c >= 32 && c < 127) ? c : '.');
        }
        System.out.println();
    }

    /**
     * Demonstrate reading KLV data using streams (like from a socket).
     */
    public static KLVMessage readKLVFromStream(InputStream input) throws IOException {
        // Step 1: Read 4 bytes for key
        byte[] keyBytes = new byte[4];
        int bytesRead = input.read(keyBytes);
        if (bytesRead != 4) {
            throw new IOException("Could not read key (expected 4 bytes)");
        }

        // Convert key bytes to string (strip null padding)
        int keyLength = 4;
        for (int i = 0; i < 4; i++) {
            if (keyBytes[i] == 0) {
                keyLength = i;
                break;
            }
        }
        String key = new String(keyBytes, 0, keyLength, StandardCharsets.US_ASCII);

        // Step 2: Read 4 bytes for length
        byte[] lengthBytes = new byte[4];
        bytesRead = input.read(lengthBytes);
        if (bytesRead != 4) {
            throw new IOException("Could not read length (expected 4 bytes)");
        }

        // Convert to integer (big-endian)
        ByteBuffer lengthBuffer = ByteBuffer.wrap(lengthBytes);
        int length = lengthBuffer.getInt();

        // Step 3: Read exactly 'length' bytes for value
        byte[] value = new byte[length];
        int totalRead = 0;
        while (totalRead < length) {
            bytesRead = input.read(value, totalRead, length - totalRead);
            if (bytesRead == -1) {
                throw new IOException("Unexpected end of stream");
            }
            totalRead += bytesRead;
        }

        return new KLVMessage(key, value);
    }

    // ========================================================================
    // EXAMPLE USAGE - Understanding the basics with GENERIC data
    // ========================================================================

    public static void main(String[] args) throws Exception {
        System.out.println("=".repeat(70));
        System.out.println("KLV Protocol Examples - Learning the Basics");
        System.out.println("=".repeat(70));

        // Example 1: Simple KLV encoding
        System.out.println("\n[Example 1] Simple KLV: USER command with username 'alice'");
        System.out.println("-".repeat(70));
        System.out.println("NOTE: 'USER' is a GENERIC example, not part of your chat protocol!");
        byte[] userMsg = encodeKLV("USER", "alice".getBytes(StandardCharsets.UTF_8));
        hexDump(userMsg, "Encoded USER:5:alice");

        // Decode it back
        KLVMessage decoded = decodeKLV(userMsg);
        System.out.println("\nDecoded:");
        System.out.println("  Key: " + decoded.key);
        System.out.println("  Value: " + new String(decoded.value, StandardCharsets.UTF_8));

        // Example 2: Key padding demonstration
        System.out.println("\n\n[Example 2] Key Padding: 3-letter key 'ACK' becomes 4 bytes");
        System.out.println("-".repeat(70));
        String ackKey = "ACK";
        System.out.println("Original key: '" + ackKey + "' (" + ackKey.length() + " chars)");

        // Show how ACK gets padded
        byte[] paddedKey = new byte[4];
        System.arraycopy(ackKey.getBytes(StandardCharsets.US_ASCII), 0, paddedKey, 0, 3);
        System.out.print("Padded key bytes: ");
        for (byte b : paddedKey) {
            System.out.printf("%02x ", b);
        }
        System.out.println("(hex)");
        System.out.println("               = [A] [C] [K] [\\0]");
        System.out.println("               = 0x41 0x43 0x4B 0x00");

        // Example 3: Big-endian length encoding
        System.out.println("\n\n[Example 3] Big-Endian Length Encoding");
        System.out.println("-".repeat(70));
        int[] lengths = {5, 32, 127, 256, 1000};
        for (int length : lengths) {
            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.putInt(length);
            byte[] lengthBytes = buffer.array();
            System.out.printf("Length %4d → bytes: ", length);
            for (byte b : lengthBytes) {
                System.out.printf("%02x ", b);
            }
            System.out.println("(hex)");
        }

        // Example 4: Nested KLV
        System.out.println("\n\n[Example 4] Nested KLV Structure");
        System.out.println("-".repeat(70));
        System.out.println("Creating: DATA with nested NAME and INFO fields");
        System.out.println("  NAME:5:alice");
        System.out.println("  INFO:12:test message");
        System.out.println("\nNOTE: This is GENERIC. Your protocol uses different keys!");

        // Build it step by step
        byte[] nameKLV = encodeKLV("NAME", "alice".getBytes(StandardCharsets.UTF_8));
        byte[] infoKLV = encodeKLV("INFO", "test message".getBytes(StandardCharsets.UTF_8));

        hexDump(nameKLV, "NAME KLV (4 + 4 + 5 = 13 bytes)");
        hexDump(infoKLV, "INFO KLV (4 + 4 + 12 = 20 bytes)");

        // Combine them
        ByteArrayOutputStream nestedValue = new ByteArrayOutputStream();
        nestedValue.write(nameKLV);
        nestedValue.write(infoKLV);
        System.out.println("\nCombined nested value: " + nestedValue.size() +
                " bytes (13 + 20)");

        // Wrap in DATA
        byte[] dataMsg = encodeKLV("DATA", nestedValue.toByteArray());
        hexDump(dataMsg, "Complete DATA:33:NAME:5:alice:INFO:12:test message");

        // Decode the nested structure
        System.out.println("\nDecoding nested structure:");
        List<KLVMessage> nestedItems = decodeNestedKLV(dataMsg);
        System.out.println("  Outer key: DATA");
        for (KLVMessage item : nestedItems) {
            System.out.println("    " + item.key + ": " +
                    new String(item.value, StandardCharsets.UTF_8));
        }

        // Example 5: Multiple KLV structures in sequence
        System.out.println("\n\n[Example 5] Parsing Multiple KLV Structures in Sequence");
        System.out.println("-".repeat(70));

        // Create a sequence
        byte[] user1 = encodeKLV("USER", "alice".getBytes(StandardCharsets.UTF_8));
        byte[] user2 = encodeKLV("USER", "bob".getBytes(StandardCharsets.UTF_8));
        byte[] quit1 = encodeKLV("QUIT", "alice".getBytes(StandardCharsets.UTF_8));

        ByteArrayOutputStream sequence = new ByteArrayOutputStream();
        sequence.write(user1);
        sequence.write(user2);
        sequence.write(quit1);

        hexDump(sequence.toByteArray(), "Sequence of 3 messages");

        // Parse them one by one
        System.out.println("\nParsing sequence:");
        int offset = 0;
        int msgNum = 1;
        byte[] seqData = sequence.toByteArray();
        while (offset + 8 <= seqData.length) {
            KLVMessage message = decodeKLV(seqData, offset);
            System.out.println("  Message " + msgNum + ": " + message);
            offset += 8 + message.value.length;
            msgNum++;
        }

        // Example 6: Status codes as ASCII strings
        System.out.println("\n\n[Example 6] Status Codes are ASCII Strings, NOT Integers!");
        System.out.println("-".repeat(70));
        System.out.println("Common mistake: encoding 200 as a binary integer");
        System.out.println("Correct approach: encode '200' as ASCII string");

        // WRONG way (binary integer)
        ByteBuffer wrongBuffer = ByteBuffer.allocate(4);
        wrongBuffer.putInt(200);
        byte[] wrong = wrongBuffer.array();
        System.out.print("\nWRONG - Binary integer: ");
        for (byte b : wrong) {
            System.out.printf("%02x ", b);
        }
        System.out.println("(4 bytes)");

        // RIGHT way (ASCII string)
        byte[] right = "200".getBytes(StandardCharsets.US_ASCII);
        System.out.print("RIGHT - ASCII string:   ");
        for (byte b : right) {
            System.out.printf("%02x ", b);
        }
        System.out.println("(3 bytes)");
        System.out.println("                        = '2' '0' '0'");

        // Example in KLV
        byte[] statusKLV = encodeKLV("STAT", "200".getBytes(StandardCharsets.US_ASCII));
        hexDump(statusKLV, "STAT:3:200 (status code as ASCII)");

        // Summary
        System.out.println("\n" + "=".repeat(70));
        System.out.println("Key Takeaways:");
        System.out.println("=".repeat(70));
        System.out.println("1. Keys are ALWAYS exactly 4 bytes (pad with \\x00)");
        System.out.println("2. Lengths are ALWAYS 4 bytes, big-endian");
        System.out.println("3. Use ByteBuffer for easy big-endian integer conversion");
        System.out.println("4. Nested structures: inner KLV is the 'value' of outer KLV");
        System.out.println("5. No delimiters - the length tells you how many bytes to read");
        System.out.println("6. Parse incrementally: key (4) → length (4) → value (length)");
        System.out.println("7. Status codes like '200' are ASCII strings, not integers!");
        System.out.println("8. When reading from sockets, read exact byte counts!");
        System.out.println("=".repeat(70));
        System.out.println("\nNOW: Apply these concepts to implement YOUR chat protocol:");
        System.out.println("  - JOIN, MSG, READ, EXIT, RESP");
        System.out.println("  - FROM, BODY, CODE, TYPE, MSGS");
        System.out.println("=".repeat(70));
    }
}