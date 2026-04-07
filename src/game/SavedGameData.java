package game;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * A flat, Gson-serializable snapshot of the full game state.
 * Every field is a primitive, String, enum name, or a nested POJO
 * so Gson can read/write it with no custom adapters.
 */
public class SavedGameData {

    // -----------------------------------------------------------------------
    // Metadata
    // -----------------------------------------------------------------------

    /** Human-readable timestamp of when the save was created. */
    public String savedAt;

    /** Schema version — bump when the structure changes incompatibly. */
    public int version = 1;

    // -----------------------------------------------------------------------
    // Game-level state
    // -----------------------------------------------------------------------

    /** The current round number (1-based). */
    public int currentRound;

    /** Index of the player whose turn it is. */
    public int currentPlayerIndex;

    /** Size of the map grid (e.g. 3, 5, 7). */
    public int mapSize;

    /**
     * Flat row-major grid snapshot: mapSize*mapSize entries, each a single char
     * ('.' = empty, 'X' = player 1 territory, 'O' = player 2 territory, etc.)
     */
    public List<String> gridCells;

    /**
     * Flat row-major bonus-cell snapshot: mapSize*mapSize booleans.
     * True if the cell contains a bonus token.
     */
    public List<Boolean> bonusCells;

    // -----------------------------------------------------------------------
    // Per-player snapshots
    // -----------------------------------------------------------------------

    public List<PlayerSnapshot> players;

    // -----------------------------------------------------------------------
    // Factory — capture
    // -----------------------------------------------------------------------

    public static SavedGameData from(GameState state, MapGrid map) {
        SavedGameData data = new SavedGameData();

        data.savedAt = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        data.currentRound       = state.getRoundNumber();
        data.currentPlayerIndex = state.getCurrentPlayerIndex();
        data.mapSize            = map.getSize();

        int size = map.getSize();

        // Serialise the map grid
        data.gridCells = new ArrayList<>(size * size);
        data.bonusCells = new ArrayList<>(size * size);
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                data.gridCells.add(String.valueOf(map.getCell(r, c)));
                data.bonusCells.add(map.hasBonus(r, c));
            }
        }

        // Serialise each player
        data.players = new ArrayList<>();
        for (player.Player p : state.getPlayers()) {
            data.players.add(PlayerSnapshot.from(p));
        }

        return data;
    }

    // -----------------------------------------------------------------------
    // Factory — restore
    // -----------------------------------------------------------------------

    /**
     * Rebuilds a {@link GameState} and {@link MapGrid} from this snapshot.
     *
     * <p>The returned array always has exactly two elements:
     * <pre>
     *   Object[] result = data.restoreInto();
     *   GameState state = (GameState) result[0];
     *   MapGrid   map   = (MapGrid)   result[1];
     * </pre>
     *
     * @return {@code [GameState, MapGrid]}
     */
    public Object[] restoreInto() {
        GameState state = new GameState();
        state.setRoundNumber(currentRound);
        state.setCurrentPlayerIndex(currentPlayerIndex);

        for (PlayerSnapshot snap : players) {
            player.Player p = new player.Player(snap.name);
            p.setSymbol(snap.symbol);
            p.setScore(snap.score);
            p.setTimer(snap.timer);
            p.setStreakRaw(snap.streak);
            p.setCoins(snap.coins);
            for (int i = 0; i < snap.bonusTokens; i++) {
                p.addBonusToken();
            }

            if (snap.isBot && snap.botDifficulty != null) {
                switch (snap.botDifficulty) {
                    case "EASY"   -> p.setStrategy(new bot.EasyBot());
                    case "MEDIUM" -> p.setStrategy(new bot.MediumBot());
                    case "HARD"   -> p.setStrategy(new bot.HardBot());
                }
            }

            if (snap.inventoryWeapons != null) {
                for (String wName : snap.inventoryWeapons) {
                    try {
                        p.addWeapon(WeaponType.valueOf(wName));
                    } catch (IllegalArgumentException ignored) {
                        // Unknown weapon type in save — silently skip
                    }
                }
            }

            state.addPlayer(p);
        }

        // Rebuild map
        MapGrid map = new MapGrid(mapSize);
        for (player.Player p : state.getPlayers()) {
            map.initVisibilityForPlayer(p.getSymbol());
        }

        int size = mapSize;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                int idx = r * size + c;
                if (idx < gridCells.size()) {
                    String cell = gridCells.get(idx);
                    char owner = cell.isEmpty() ? '.' : cell.charAt(0);
                    if (owner != '.') {
                        map.setOwner(r, c, owner);
                        map.revealNeighbourForPlayer(owner, r, c);
                    }
                }
            }
        }
        // Note: bonus_cells in MapGrid are final/random; we restore the grid
        // ownership but the bonus cell layout is regenerated fresh.

        return new Object[]{state, map};
    }

    // -----------------------------------------------------------------------
    // Inner POJO: per-player snapshot
    // -----------------------------------------------------------------------

    public static class PlayerSnapshot {

        public String name;
        public char   symbol;
        public int    score;
        public long   timer;
        public int    streak;
        public int    bonusTokens;
        public int    coins;
        public int    correctAnswers;
        public int    wrongAnswers;
        public boolean isBot;
        /** Bot difficulty: "EASY", "MEDIUM", "HARD", or null for humans. */
        public String  botDifficulty;
        /** Names of WeaponType enum constants in the player's inventory. */
        public List<String> inventoryWeapons;

        public static PlayerSnapshot from(player.Player p) {
            PlayerSnapshot snap = new PlayerSnapshot();
            snap.name           = p.getName();
            snap.symbol         = p.getSymbol();
            snap.score          = p.getScore();
            snap.timer          = p.getTimer();
            snap.streak         = p.getStreak();
            snap.bonusTokens    = p.getBonusTokens();
            snap.coins          = p.getCoins();
            snap.correctAnswers = p.getCorrectAnswers();
            snap.wrongAnswers   = p.getWrongAnswers();
            snap.isBot          = p.isBot();

            if (p.isBot() && p.getStrategy() != null) {
                snap.botDifficulty = p.getStrategy().getDifficultyName().toUpperCase();
            }

            snap.inventoryWeapons = new ArrayList<>();
            for (WeaponType wt : p.getInventory()) {
                snap.inventoryWeapons.add(wt.name());
            }

            return snap;
        }
    }
}
