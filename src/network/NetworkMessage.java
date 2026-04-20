package network;

import java.util.List;

/**
 * Wire format for messages exchanged between the server and its clients
 * (#87). Serialised to JSON (one message per line) by Gson. A single POJO
 * shape is used in both directions; unused fields are left null.
 *
 * <p>
 * Message {@link #type} names come from {@link Type}. The server drives
 * turn state and broadcasts {@code PHASE}, {@code QUESTION}, {@code RESULT},
 * {@code SCORES}, {@code TURN} and {@code GAME_OVER}. Clients send
 * {@code JOIN}, {@code READY} and {@code ANSWER}.
 * </p>
 */
public class NetworkMessage {

    public enum Type {
        // Client -> Server
        JOIN,
        READY,
        ANSWER,
        START_GAME,
        // Server -> Client
        WELCOME,
        LOBBY,
        PHASE,
        QUESTION,
        RESULT,
        SCORES,
        TURN,
        GAME_OVER,
        ERROR,
        // chat box
        CHAT
    }

    public Type type;

    public String text;
    public Integer senderIndex;

    // Identification
    public String name;
    public Integer playerIndex;

    // Phase / turn
    public String phase;
    public Integer currentPlayer;
    public Integer round;
    public Integer totalRounds;

    // Question payload
    public String questionType;
    public String category;
    public String difficulty;
    public String prompt;
    public List<String> choices;

    // Answer payload
    public String answer;
    public Long elapsedMs;

    // Result payload
    public Boolean correct;
    public Boolean timedOut;
    public Integer pointsDelta;
    public String correctAnswer;

    // Score payload
    public List<Integer> scores;
    public List<String> playerNames;

    // Game over
    public Integer winnerIndex;

    // Error
    public String errorMessage;

    public NetworkMessage() {
    }

    public NetworkMessage(Type type) {
        this.type = type;
    }

    public static NetworkMessage join(String name) {
        NetworkMessage m = new NetworkMessage(Type.JOIN);
        m.name = name;
        return m;
    }

    public static NetworkMessage ready() {
        return new NetworkMessage(Type.READY);
    }

    public static NetworkMessage answer(String answer, long elapsedMs) {
        NetworkMessage m = new NetworkMessage(Type.ANSWER);
        m.answer = answer;
        m.elapsedMs = elapsedMs;
        return m;
    }

    public static NetworkMessage error(String msg) {
        NetworkMessage m = new NetworkMessage(Type.ERROR);
        m.errorMessage = msg;
        return m;
    }
}
