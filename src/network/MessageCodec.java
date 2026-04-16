package network;

import com.google.gson.Gson;

/**
 * Tiny helper that hides the Gson dependency behind a pair of static
 * methods. Each encoded line ends in {@code \n} so readers can use
 * {@link java.io.BufferedReader#readLine()}.
 */
public final class MessageCodec {

    private static final Gson GSON = new Gson();

    private MessageCodec() {
    }

    public static String encode(NetworkMessage msg) {
        return GSON.toJson(msg);
    }

    public static NetworkMessage decode(String line) {
        if (line == null || line.isBlank()) return null;
        return GSON.fromJson(line, NetworkMessage.class);
    }
}
