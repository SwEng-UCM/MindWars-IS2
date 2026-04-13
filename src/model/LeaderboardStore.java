package model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Persistent leaderboard storage (#89). Reads/writes a JSON file next to
 * the working directory and exposes {@link #recordResult(String, int, boolean)}
 * so the controller can log an end-of-game outcome.
 *
 * <p>
 * The on-disk format is a plain JSON array of {@link LeaderboardEntry}
 * objects — easy to inspect, simple to extend.
 */
public class LeaderboardStore {

    private static final Path DEFAULT_FILE = Paths.get("leaderboard.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LIST_TYPE = new TypeToken<List<LeaderboardEntry>>() {
    }.getType();

    private final Path file;
    private final Map<String, LeaderboardEntry> entries = new HashMap<>();

    public LeaderboardStore() {
        this(DEFAULT_FILE);
    }

    public LeaderboardStore(Path file) {
        this.file = file;
        load();
    }

    /** Re-reads the JSON file from disk, discarding any in-memory state. */
    public void reload() {
        entries.clear();
        load();
    }

    /** Returns a snapshot of all entries, sorted best first. */
    public List<LeaderboardEntry> getEntries() {
        List<LeaderboardEntry> list = new ArrayList<>(entries.values());
        Collections.sort(list);
        return list;
    }

    /**
     * Records one player's result from a finished game. {@code scoreGained}
     * is the total points they earned in that game; {@code won} is whether
     * they won it (draws count as no winner).
     */
    public void recordResult(String playerName, int scoreGained, boolean won) {
        if (playerName == null || playerName.isBlank())
            return;
        LeaderboardEntry e = entries.computeIfAbsent(playerName, LeaderboardEntry::new);
        e.recordGame(scoreGained, won);
        save();
    }

    // ── Persistence ──

    private void load() {
        if (!Files.exists(file))
            return;
        try {
            String json = Files.readString(file);
            List<LeaderboardEntry> list = GSON.fromJson(json, LIST_TYPE);
            if (list != null) {
                for (LeaderboardEntry e : list) {
                    if (e != null && e.getName() != null) {
                        entries.put(e.getName(), e);
                    }
                }
            }
        } catch (IOException | com.google.gson.JsonSyntaxException ex) {
            // Corrupt or unreadable file — start fresh rather than crashing.
            entries.clear();
        }
    }

    private void save() {
        try {
            String json = GSON.toJson(getEntries(), LIST_TYPE);
            Files.writeString(file, json);
        } catch (IOException ex) {
            // Non-fatal: leaderboard persistence is best-effort.
        }
    }
}
