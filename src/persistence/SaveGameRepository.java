package persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SaveGameRepository {

    public void saveGame(
            String saveName,
            int player1UserId,
            int player2UserId,
            Integer currentTurnUserId,
            int mapSize,
            String gameMode,
            String phase,
            String gameStateJson) {
        String query = """
                    INSERT INTO saved_games
                    (save_name, player1_user_id, player2_user_id, current_turn_user_id,
                     map_size, game_mode, phase, game_state_json)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query);) {
            stmt.setString(1, saveName);
            stmt.setInt(2, player1UserId);
            stmt.setInt(3, player2UserId);

            if (currentTurnUserId == null) {
                stmt.setNull(4, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(4, currentTurnUserId);
            }

            stmt.setInt(5, mapSize);
            stmt.setString(6, gameMode);
            stmt.setString(7, phase);
            stmt.setString(8, gameStateJson);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save game", e);
        }

    }
}
