package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

/**
 * Thin TCP client that talks to a {@link GameServer} over newline-delimited
 * JSON (#87). It does not own any game state of its own — every authoritative
 * update arrives as a broadcast from the server and is dispatched to the
 * consumer registered via {@link #setListener(Consumer)}.
 *
 * <p>Typical usage:</p>
 * <pre>
 * GameClient c = new GameClient();
 * c.setListener(msg -&gt; ...);
 * c.connect("192.168.1.10", 5555, "Alice");
 * c.sendReady();
 * c.sendAnswer("B", 2400);
 * </pre>
 */
public class GameClient {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Thread readerThread;
    private volatile boolean running;

    private Consumer<NetworkMessage> listener = msg -> {};

    public void setListener(Consumer<NetworkMessage> listener) {
        this.listener = listener == null ? (msg -> {}) : listener;
    }

    public boolean isConnected() {
        return running && socket != null && !socket.isClosed();
    }

    /**
     * Opens the socket, sends the initial {@code JOIN} with the given name,
     * and spins up a background reader. Throws if the connection cannot be
     * established so callers can surface the failure to the user.
     */
    public void connect(String host, int port, String playerName) throws IOException {
        this.socket = new Socket(host, port);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.running = true;
        this.readerThread = new Thread(this::readLoop, "MindWars-Client-Reader");
        this.readerThread.setDaemon(true);
        this.readerThread.start();
        send(NetworkMessage.join(playerName));
    }

    public void sendReady() {
        send(NetworkMessage.ready());
    }

    public void sendAnswer(String answer, long elapsedMs) {
        send(NetworkMessage.answer(answer, elapsedMs));
    }

    public void send(NetworkMessage msg) {
        if (out == null) return;
        out.println(MessageCodec.encode(msg));
    }

    public void close() {
        running = false;
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {
        }
    }

    private void readLoop() {
        try {
            String line;
            while (running && (line = in.readLine()) != null) {
                NetworkMessage msg = MessageCodec.decode(line);
                if (msg != null) listener.accept(msg);
            }
        } catch (IOException e) {
            if (running) System.err.println("[client] read failed: " + e.getMessage());
        } finally {
            running = false;
        }
    }
}
