package persistence;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {

        try (
                Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute("""

                    CREATE TABLE IF NOT EXISTS users(
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username text NOT NULL UNIQUE,
                        email texT NOT NULL UNIQUE,
                        password_hash text NOT NULL,
                        created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP


                    )

                    """

            );
            stmt.execute("""
                                CREATE TABLE IF NOT EXISTS saved_games(
                                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                                    save_name TEXT NOT NULL,
                                    map_size INTEGER NOT NULL,
                                    game_mode TEXT NOT NULL,
                                    phase  TEXT NOT NULL,
                                    current_turn_user_id INTEGER,
                                    player1_user_id INTEGER NOT NULL,
                                    player2_user_id INTEGER NOT NULL,
                                    game_state_json TEXT NOT NULL,
                                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    FOREIGN KEY (player1_user_id) REFERENCES users(id),
                                    FOREIGN KEY (player2_user_id) REFERENCES users(id),
                                    FOREIGN KEY (current_turn_user_id) REFERENCES users(id)


                                )

                    """);
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS leaderboard_entries (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            user_id INTEGER NOT NULL,
                            score INTEGER NOT NULL,
                            territories_owned INTEGER NOT NULL,
                            correct_answers INTEGER NOT NULL DEFAULT 0,
                            wrong_answers INTEGER NOT NULL DEFAULT 0,
                            average_response_time_ms REAL NOT NULL DEFAULT 0,
                            fastest_response_time_ms REAL NOT NULL DEFAULT 0,
                            won INTEGER NOT NULL DEFAULT 0,
                            game_mode TEXT,
                            map_size INTEGER,
                            played_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (user_id) REFERENCES users(id)
                        )
                    """);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize the database", e);
        }
    }
}
