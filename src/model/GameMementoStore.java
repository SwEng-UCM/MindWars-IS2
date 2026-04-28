package model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Caretaker (GoF Memento) for {@link GameMemento}. Reads and writes a
 * single save slot at {@code ~/MindWars/saves/slot.mwsave} as
 * pretty-printed JSON.
 */
public final class GameMementoStore {

    private static final Path SAVE_DIR =
            Paths.get(System.getProperty("user.home"), "MindWars", "saves");
    private static final Path SLOT_PATH = SAVE_DIR.resolve("slot.mwsave");

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public boolean hasSave() {
        return Files.exists(SLOT_PATH);
    }

    public void save(GameMemento m) throws IOException {
        Files.createDirectories(SAVE_DIR);
        Files.writeString(SLOT_PATH, GSON.toJson(m), StandardCharsets.UTF_8);
    }

    /** Returns the saved memento, or {@code null} if no save exists. */
    public GameMemento load() throws IOException {
        if (!hasSave()) return null;
        String json = Files.readString(SLOT_PATH, StandardCharsets.UTF_8);
        GameMemento m = GSON.fromJson(json, GameMemento.class);
        if (m == null || m.version != GameMemento.CURRENT_VERSION) {
            throw new IOException("Save file is from an incompatible version.");
        }
        return m;
    }

    public void delete() throws IOException {
        Files.deleteIfExists(SLOT_PATH);
    }

    public Path getSlotPath() {
        return SLOT_PATH;
    }
}
