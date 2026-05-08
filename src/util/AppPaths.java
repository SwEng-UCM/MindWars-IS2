/*
 * @author Leopold Popper
 * AI-assisted: yes (Claude by Anthropic, via Claude Code)
 */
package util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Resolves writable file locations for the packaged JAR build, so the
 * game does not depend on the current working directory.
 */
public final class AppPaths {

    private static final Path USER_DIR =
            Paths.get(System.getProperty("user.home"), "MindWars");

    private AppPaths() {}

    public static Path userDir() {
        try {
            Files.createDirectories(USER_DIR);
        } catch (Exception ignored) {
        }
        return USER_DIR;
    }

    public static Path leaderboardFile() {
        return userDir().resolve("leaderboard.json");
    }

    public static Path databaseFile() {
        return userDir().resolve("mindwars.db");
    }
}
