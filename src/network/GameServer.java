package network;

import model.AnswerResult;
import model.GameModel;
import model.GamePhase;
import model.GameSettings;
import player.Player;
import trivia.Question;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Authoritative game server for multiplayer play (#87).
 *
 * <p>
 * Accepts up to {@link GameSettings#numPlayers} TCP connections on a
 * chosen port. Each connected client sends a {@code JOIN} message with
 * their display name; once every seat is filled, the server starts a game
 * using the {@link GameSettings} it was built with and begins broadcasting
 * turn state to both clients.
 * </p>
 *
 * <p>
 * The server is the only place that runs the {@link GameModel}. Clients
 * cannot advance the phase themselves — they send {@code READY} / {@code
 * ANSWER} messages and the server decides when to move on. Every phase
 * transition fires a {@code PHASE}/{@code QUESTION}/{@code SCORES}/
 * {@code TURN}/{@code GAME_OVER} broadcast so both clients stay in sync.
 * </p>
 */
public class GameServer {

    public static final int DEFAULT_PORT = 5555;
    public static final int MAX_PLAYERS = 4;

    private final int port;
    private GameSettings settings;
    private final GameModel model;

    private ServerSocket serverSocket;
    private Thread acceptThread;

    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private volatile boolean running;

    /**
     * Tracks which players have sent READY for the current HOT_SEAT_PASS /
     * INVASION_PASS phase. Cleared on every phase change.
     */
    private final boolean[] readyFlags;

    /**
     * The in-flight answer submitted by each player index for the current question.
     */
    private final NetworkMessage[] pendingAnswers;
    private int[] pickOrder;

    private int pickIndex;

    public GameServer(int port, GameSettings settings, GameModel model) {
        this.port = port;
        this.settings = settings;
        this.model = model;

        int playerCount = settings.numPlayers;

        this.readyFlags = new boolean[playerCount];
        this.pendingAnswers = new NetworkMessage[playerCount];

        model.addPropertyChangeListener(evt -> {
            if (GameModel.PROP_PHASE.equals(evt.getPropertyName())) {
                onPhaseChanged((GamePhase) evt.getNewValue());
            }
        });
    }

    /**
     * Returns the port the server is actually bound to (useful when 0 was
     * requested).
     */
    public int getBoundPort() {
        return serverSocket == null ? port : serverSocket.getLocalPort();
    }

    public boolean isRunning() {
        return running;
    }

    public int getConnectedCount() {
        return clients.size();
    }

    /**
     * Opens the server socket and starts the accept loop on a background thread.
     */
    public void start() throws IOException {
        if (running)
            return;
        serverSocket = new ServerSocket(port);
        running = true;
        acceptThread = new Thread(this::acceptLoop, "MindWars-ServerAccept");
        acceptThread.setDaemon(true);
        acceptThread.start();
    }

    public void stop() {
        running = false;
        for (ClientHandler h : clients)
            h.close();
        clients.clear();
        try {
            if (serverSocket != null)
                serverSocket.close();
        } catch (IOException ignored) {
        }
    }

    private void acceptLoop() {
        while (running) {
            try {
                Socket sock = serverSocket.accept();
                // if the game has already started, reject new clients by closing the socket
                if (model.getPhase() != GamePhase.SETUP) {
                    sock.close();
                    continue;
                }
                ClientHandler handler = new ClientHandler(sock);
                handler.start();
            } catch (IOException e) {
                if (running)
                    System.err.println("[server] accept failed: " + e.getMessage());
                break;
            }
        }
    }

    // ── Per-client wire handler ──

    private final class ClientHandler {

        private final Socket socket;
        private final BufferedReader in;
        private final PrintWriter out;
        private final Thread readerThread;
        private volatile int seatIndex = -1;
        private volatile String displayName = "";

        ClientHandler(Socket sock) throws IOException {
            this.socket = sock;
            this.in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            this.out = new PrintWriter(sock.getOutputStream(), true);
            this.readerThread = new Thread(this::readLoop, "MindWars-Client-" + sock.getPort());
            this.readerThread.setDaemon(true);
        }

        void start() {
            readerThread.start();
        }

        void send(NetworkMessage msg) {
            out.println(MessageCodec.encode(msg));
        }

        void close() {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }

        private void readLoop() {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    NetworkMessage msg = MessageCodec.decode(line);
                    if (msg == null || msg.type == null)
                        continue;
                    handle(msg);
                }
            } catch (IOException ignored) {
            } finally {
                boolean wasMember = clients.remove(this);
                if (wasMember && seatIndex >= 0) {
                    broadcastPlayerLeft(seatIndex, displayName);
                    broadcastLobby();
                }
            }
        }

        private void handle(NetworkMessage msg) {
            switch (msg.type) {
                case JOIN -> onJoin(msg);
                case READY -> onReady();
                case ANSWER -> onAnswer(msg);
                case CLAIM_CELL -> onClaimCell(msg);
                case START_GAME -> {
                    if (seatIndex == 0) {
                        synchronized (GameServer.this) {
                            settings = withJoinedNames(settings, clients);
                            model.startGame(settings);
                        }
                    }
                }
                case CHAT -> {
                    msg.senderIndex = seatIndex;
                    msg.name = this.displayName;
                    broadcast(msg);
                }
                default -> send(NetworkMessage.error("unsupported client message: " + msg.type));
            }
        }

        private void onJoin(NetworkMessage msg) {
            synchronized (GameServer.this) {
                int limit = (settings != null && settings.numPlayers > 0)
                        ? settings.numPlayers
                        : MAX_PLAYERS;
                if (clients.size() >= limit) {
                    send(NetworkMessage.error("Server is full. Maximum " + limit + " players allowed."));
                    new Thread(() -> {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ignored) {
                        }
                        close();
                    }).start();
                    return;
                }

                seatIndex = clients.size();
                displayName = msg.name == null ? ("Player " + (seatIndex + 1)) : msg.name;
                clients.add(this);

                NetworkMessage welcome = new NetworkMessage(NetworkMessage.Type.WELCOME);
                welcome.playerIndex = seatIndex;
                welcome.totalRounds = settings.mapSize;
                send(welcome);

                broadcastLobby();
            }
        }

        private void onReady() {
            if (seatIndex < 0)
                return;
            synchronized (GameServer.this) {
                GamePhase phase = model.getPhase();
                if (phase != GamePhase.HOT_SEAT_PASS && phase != GamePhase.INVASION_PASS) {
                    return;
                }
                // For HOT_SEAT_PASS the active player is currentPlayerIndex.
                // For INVASION_PASS the active player is invaderIndex.
                int expectedSeat = (phase == GamePhase.INVASION_PASS)
                        ? model.getInvaderIndex()
                        : model.getCurrentPlayerIndex();
                if (seatIndex != expectedSeat) {
                    return;
                }
                readyFlags[seatIndex] = true;
                if (phase == GamePhase.HOT_SEAT_PASS) {
                    model.beginQuestion();
                } else {
                    model.beginInvasionSelect();
                }
            }
        }

        private void onAnswer(NetworkMessage msg) {
            if (seatIndex < 0)
                return;
            synchronized (GameServer.this) {
                GamePhase phase = model.getPhase();
                if (phase != GamePhase.QUESTION) {
                    send(NetworkMessage.error("no question in progress"));
                    return;
                }
                if (seatIndex != model.getCurrentPlayerIndex()) {
                    send(NetworkMessage.error("not your turn"));
                    return;
                }
                pendingAnswers[seatIndex] = msg;
                long elapsed = msg.elapsedMs == null ? 0L : msg.elapsedMs;
                AnswerResult result = model.submitAnswer(msg.answer, elapsed);
                broadcastResult(result, seatIndex);
                broadcastScores();
            }
            // Give clients time to render the RESULT feedback before the next
            // PHASE/QUESTION broadcast overwrites it. Scheduled outside the
            // synchronized block so the server thread isn't blocked.
            new Thread(() -> {
                try {
                    Thread.sleep(1600);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                synchronized (GameServer.this) {
                    if (model.getPhase() == GamePhase.QUESTION) {
                        model.advanceAfterAnswer();
                    }
                }
            }, "MindWars-PostAnswer").start();
        }

        private void onClaimCell(NetworkMessage msg) {
            if (seatIndex < 0 || msg.row == null || msg.col == null)
                return;
            synchronized (GameServer.this) {
                // Must be in TERRITORY_CLAIM phase
                if (model.getPhase() != GamePhase.TERRITORY_CLAIM) {
                    send(NetworkMessage.error("not in territory claim phase"));
                    return;
                }
                if (pickOrder == null || pickIndex >= pickOrder.length
                        || pickOrder[pickIndex] != seatIndex) {
                    send(NetworkMessage.error("not your turn to claim"));
                    return;
                }
                // Try to claim the cell via the model
                boolean ok = model.claimCell(seatIndex, msg.row, msg.col);
                if (!ok) {
                    send(NetworkMessage.error("cell already taken"));
                    return;
                }
                pickIndex++;

                // Broadcast updated map to all clients
                broadcastMapUpdate();

                // If all picks exhausted (or map full), finish the round
                if (pickIndex >= pickOrder.length || model.getMap().isMapFull()) {
                    model.finishRound();
                }
            }
        }
    }

    private void buildPickOrder() {
        int[] claimCounts = model.roundClaimCounts();
        int winner = model.determineRoundWinnerIndex();
        int numPlayers = claimCounts.length;

        int total = 0;
        for (int c : claimCounts)
            total += c;

        pickOrder = new int[total];
        int idx = 0;
        for (int i = 0; i < claimCounts[winner]; i++)
            pickOrder[idx++] = winner;
        for (int p = 0; p < numPlayers; p++) {
            if (p == winner)
                continue;
            for (int i = 0; i < claimCounts[p]; i++)
                pickOrder[idx++] = p;
        }
        pickIndex = 0;
    }

    // ── Broadcasts ──

    private synchronized void onPhaseChanged(GamePhase phase) {
        // Reset ready flags on every phase transition so they don't leak.
        for (int i = 0; i < readyFlags.length; i++)
            readyFlags[i] = false;

        NetworkMessage m = new NetworkMessage(NetworkMessage.Type.PHASE);
        m.phase = phase.name();
        // For invasion phases, currentPlayer must be the invader index, not
        // getCurrentPlayerIndex() which is always 0 after a reset.
        if (phase == GamePhase.INVASION_PASS || phase == GamePhase.INVASION_BATTLE) {
            m.currentPlayer = model.getInvaderIndex();
        } else {
            m.currentPlayer = model.getCurrentPlayerIndex();
        }
        m.round = model.getRoundNumber();
        m.totalRounds = model.getTotalRounds();
        broadcast(m);

        switch (phase) {
            case QUESTION -> broadcastQuestion();
            case TERRITORY_CLAIM -> {
                buildPickOrder();
                broadcastMapUpdate();
            }
            case INVASION_PASS, INVASION_SELECT, INVASION_BATTLE -> {
                model.forcePhase(GamePhase.GAME_OVER);
            }
            case GAME_OVER -> broadcastGameOver();
            default -> {
            }
        }
    }

    private void broadcastQuestion() {
        Question q = model.getCurrentQuestion();
        if (q == null)
            return;
        NetworkMessage m = new NetworkMessage(NetworkMessage.Type.QUESTION);
        m.questionType = q.getType() == null ? null : q.getType().name();
        m.category = q.getCategory();
        m.difficulty = q.getDifficulty();
        m.prompt = q.getPrompt();
        m.choices = q.getChoices() == null ? null : new ArrayList<>(q.getChoices());
        m.currentPlayer = model.getCurrentPlayerIndex();
        broadcast(m);
    }

    private void broadcastResult(AnswerResult result, int playerIndex) {
        NetworkMessage m = new NetworkMessage(NetworkMessage.Type.RESULT);
        m.playerIndex = playerIndex;
        m.correct = result.correct;
        m.timedOut = result.timedOut;
        m.pointsDelta = result.pointsDelta;
        m.correctAnswer = result.correctAnswer;
        m.elapsedMs = result.elapsedMs;
        for (ClientHandler h : clients) {
            if (h.seatIndex == playerIndex) {
                h.send(m);
                break;
            }
        }
    }

    private void broadcastScores() {
        NetworkMessage m = new NetworkMessage(NetworkMessage.Type.SCORES);
        List<Integer> scores = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (ClientHandler h : clients) {
            names.add(h.displayName);
        }
        for (Player p : model.getPlayers()) {
            scores.add(p.getScore());
            // names.add(p.getName());
        }
        m.scores = scores;
        m.playerNames = names;
        broadcast(m);
    }

    private void broadcastGameOver() {
        Player winner = model.computeWinner();
        NetworkMessage m = new NetworkMessage(NetworkMessage.Type.GAME_OVER);
        m.winnerIndex = winner == null ? null : model.getPlayers().indexOf(winner);

        List<String> names = new ArrayList<>();
        List<Integer> scores = new ArrayList<>();
        List<Integer> corrects = new ArrayList<>();
        List<Integer> wrongs = new ArrayList<>();
        for (Player p : model.getPlayers()) {
            names.add(p.getName());
            scores.add(p.getScore());
            corrects.add(p.getCorrectAnswers());
            wrongs.add(p.getWrongAnswers());
        }
        m.playerNames = names;
        m.scores = scores;
        m.correctAnswers = corrects;
        m.wrongAnswers = wrongs;

        game.MapGrid map = model.getMap();
        if (map != null) {
            int size = map.getSize();
            StringBuilder sb = new StringBuilder(size * size);
            for (int r = 0; r < size; r++)
                for (int c = 0; c < size; c++)
                    sb.append(map.getOwner(r, c));
            m.gridSnapshot = sb.toString();
            m.mapSize = size;
        }

        broadcast(m);
    }

    private void broadcastPlayerLeft(int seatIndex, String name) {
        NetworkMessage m = new NetworkMessage(NetworkMessage.Type.PLAYER_LEFT);
        m.disconnectedPlayerIndex = seatIndex;
        m.disconnectedPlayerName = name == null || name.isBlank() ? "Player " + (seatIndex + 1) : name;
        broadcast(m);
    }

    private void broadcastLobby() {
        NetworkMessage m = new NetworkMessage(NetworkMessage.Type.LOBBY);
        List<String> names = new ArrayList<>();
        for (ClientHandler h : clients)
            names.add(h.displayName);
        m.playerNames = names;
        broadcast(m);
    }

    private void broadcast(NetworkMessage msg) {
        String encoded = MessageCodec.encode(msg);
        for (ClientHandler h : clients) {
            h.out.println(encoded);
        }
    }

    private void broadcastMapUpdate() {
        game.MapGrid map = model.getMap();
        if (map == null)
            return;

        int size = map.getSize();
        StringBuilder sb = new StringBuilder(size * size);
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                sb.append(map.getOwner(r, c));

        NetworkMessage m = new NetworkMessage(NetworkMessage.Type.MAP_UPDATE);
        m.gridSnapshot = sb.toString();
        m.mapSize = size;

        if (pickIndex >= pickOrder.length || map.isMapFull()) {
            m.claimInstruction = "All territories claimed!";
            m.claimingPlayer = -1;
            m.claimsLeft = 0;
        } else {
            int nextPlayer = pickOrder[pickIndex];
            // Count consecutive picks for nextPlayer
            int left = 0;
            for (int i = pickIndex; i < pickOrder.length && pickOrder[i] == nextPlayer; i++)
                left++;

            List<Player> players = model.getPlayers();
            String pName = (nextPlayer < players.size()) ? players.get(nextPlayer).getName()
                    : ("Player " + (nextPlayer + 1));
            m.claimInstruction = pName + " — choose " + left + (left == 1 ? " territory" : " territories");
            m.claimingPlayer = nextPlayer;
            m.claimsLeft = left;
        }

        broadcast(m);
    }

    private static GameSettings withJoinedNames(GameSettings base, List<ClientHandler> clients) {
        int count = clients.size();
        String p1 = clients.get(0).displayName;
        String p2 = (count >= 2) ? clients.get(1).displayName : base.player2Name;
        String p3 = (count >= 3) ? clients.get(2).displayName : base.player3Name;
        String p4 = (count >= 4) ? clients.get(3).displayName : base.player4Name;

        return new GameSettings(
                base.mapSize,
                base.vsBot,
                p1, p2, p3, p4,
                base.randomMode,
                base.category,
                base.difficulty,
                count);
    }
}