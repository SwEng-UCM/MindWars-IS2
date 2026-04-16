package network;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Holds the active {@link GameServer} / {@link GameClient} for the current
 * process so that views can share them (#85). The setup screen attaches the
 * server/client once a connection is live; the lobby and gameplay screens
 * read them back through the {@link controller.GameController}.
 */
public class NetworkSession {

    private GameServer server;
    private GameClient client;
    private final List<Consumer<List<String>>> lobbyListeners = new ArrayList<>();

    public void attachServer(GameServer server) {
        this.server = server;
    }

    public void attachClient(GameClient client) {
        this.client = client;
    }

    public GameServer getServer() {
        return server;
    }

    public GameClient getClient() {
        return client;
    }

    public boolean isHost() {
        return server != null;
    }

    public boolean isConnected() {
        return client != null && client.isConnected();
    }

    public void addLobbyListener(Consumer<List<String>> listener) {
        lobbyListeners.add(listener);
    }

    public void removeLobbyListener(Consumer<List<String>> listener) {
        lobbyListeners.remove(listener);
    }

    /** Called by network message handlers when a LOBBY broadcast arrives. */
    public void fireLobbyUpdate(List<String> playerNames) {
        for (Consumer<List<String>> l : lobbyListeners) l.accept(playerNames);
    }

    public void disconnect() {
        if (client != null) client.close();
        if (server != null) server.stop();
        client = null;
        server = null;
    }
}
