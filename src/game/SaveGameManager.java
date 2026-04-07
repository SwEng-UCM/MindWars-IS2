package game;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/**
 * Reads and writes {@link SavedGameData} snapshots to/from JSON files on disk.
 *
 * <p>Two entry-points cover the full save/load lifecycle:
 * <ul>
 *   <li>{@link #saveWithDialog(SavedGameData, java.awt.Component)} — prompts the user
 *       with a file-chooser and writes the JSON.</li>
 *   <li>{@link #loadWithDialog(java.awt.Component)} — prompts the user to pick an
 *       existing save and returns the parsed {@link SavedGameData}.</li>
 * </ul>
 *
 * <p>All file operations are intentionally synchronous and run on the calling
 * thread. Callers that need non-blocking behaviour should wrap in a
 * {@link SwingWorker}.
 */
public class SaveGameManager {

    // -----------------------------------------------------------------------
    // Constants
    // -----------------------------------------------------------------------

    /** Default directory where saves land (user home / MindWars). */
    private static final Path DEFAULT_SAVE_DIR =
            Paths.get(System.getProperty("user.home"), "MindWars", "saves");

    private static final String EXTENSION       = "mwsave";
    private static final String EXTENSION_DESC  = "MindWars Save File (*." + EXTENSION + ")";

    // -----------------------------------------------------------------------
    // Gson (pretty-printed for readability; easy to swap for compact)
    // -----------------------------------------------------------------------

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Opens a Save-dialog, lets the user choose a file location, then writes
     * {@code data} as pretty-printed JSON.
     *
     * @param data   the snapshot to persist
     * @param parent the AWT component to use as dialog parent (may be null)
     * @return {@code true} if the file was written, {@code false} if the user
     *         cancelled or an error occurred
     */
    public static boolean saveWithDialog(SavedGameData data, java.awt.Component parent) {
        ensureSaveDir();

        JFileChooser chooser = buildChooser();
        chooser.setSelectedFile(
                DEFAULT_SAVE_DIR.resolve(
                        "mindwars_save_" + sanitizeTimestamp(data.savedAt) + "." + EXTENSION
                ).toFile()
        );
        chooser.setDialogTitle("Save Game");

        int result = chooser.showSaveDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION) {
            return false; // user cancelled
        }

        File file = ensureExtension(chooser.getSelectedFile());

        try {
            String json = GSON.toJson(data);
            Files.writeString(file.toPath(), json, StandardCharsets.UTF_8);
            return true;
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    parent,
                    "Could not write save file:\n" + ex.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
    }

    /**
     * Opens a Load-dialog and parses the chosen {@code .mwsave} file.
     *
     * @param parent the AWT component to use as dialog parent (may be null)
     * @return the parsed {@link SavedGameData}, or {@code null} if the user
     *         cancelled or the file could not be parsed
     */
    public static SavedGameData loadWithDialog(java.awt.Component parent) {
        ensureSaveDir();

        JFileChooser chooser = buildChooser();
        chooser.setDialogTitle("Load Save Game");

        int result = chooser.showOpenDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        File file = chooser.getSelectedFile();
        try {
            String json = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            return GSON.fromJson(json, SavedGameData.class);
        } catch (IOException | com.google.gson.JsonSyntaxException ex) {
            JOptionPane.showMessageDialog(
                    parent,
                    "Could not read save file:\n" + ex.getMessage(),
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return null;
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static JFileChooser buildChooser() {
        JFileChooser chooser = new JFileChooser(DEFAULT_SAVE_DIR.toFile());
        chooser.setFileFilter(new FileNameExtensionFilter(EXTENSION_DESC, EXTENSION));
        chooser.setAcceptAllFileFilterUsed(false);
        return chooser;
    }

    private static File ensureExtension(File file) {
        String name = file.getName();
        if (!name.endsWith("." + EXTENSION)) {
            return new File(file.getParent(), name + "." + EXTENSION);
        }
        return file;
    }

    private static void ensureSaveDir() {
        try {
            Files.createDirectories(DEFAULT_SAVE_DIR);
        } catch (IOException ignored) {
            // Best-effort; the chooser will still open to whatever is accessible
        }
    }

    private static String sanitizeTimestamp(String ts) {
        // "2025-04-06 14:32:00" -> "2025-04-06_14-32-00"
        return ts == null ? "unknown" : ts.replace(" ", "_").replace(":", "-");
    }
}
