package persistence;

import player.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LeaderBoardRepository {

    public void saveMatchResults(Player player, boolean won, int territoriesOwned, String gameMode, int mapSize) {
        String query = """
                INSERT INTO leaderboard_entries
                (user_id, score, territories_owned, correct_answers, wrong_answers,
                             average_response_time_ms, fastest_response_time_ms, won, game_mode, map_size)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            if (player.getUserID() == null) {
                throw new IllegalArgumentException("Player has no userId. Cannot save leaderboard entry.");
            }
            stmt.setInt(1, player.getUserID());
            stmt.setInt(2, player.getScore());
            stmt.setInt(3, territoriesOwned);
            stmt.setInt(4, player.getCorrectAnswers());
            stmt.setInt(5, player.getWrongAnswers());
            stmt.setDouble(6, player.getAverageResponseTime());
            stmt.setDouble(7, player.getFastestResponse());
            stmt.setInt(8, won ? 1 : 0);
            stmt.setString(9, gameMode);
            stmt.setInt(10, mapSize);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save leaderbord entry", e);
        }
    }
}
