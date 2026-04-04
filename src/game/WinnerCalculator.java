package game;

import player.Player;

import java.util.List;

/**
 * PURPOSE:
 * - Computes the final winner from player scores.
 * - Pure logic utility (no state, no I/O).
 *
 * @TODO (MVP):
 *       - getWinnerOrNull(players):
 *       - return the player with highest score
 *       - if tie for highest score, return null
 *       - if empty list, return null
 *
 * @TODO (optional later):
 *       - Return list of winners for ties instead of null.
 */

public final class WinnerCalculator {
    private WinnerCalculator() {
    }

    public static Player getWinnerOrNull(List<Player> players, MapGrid mapGrid) {
        // check if the list is empty
        if (players == null || players.isEmpty()) {
            return null;
        }

        // find the player with the highest score
        Player winner = players.get(0);
        for (int i = 1; i < players.size(); i++) {
            Player currentPlayer = players.get(i);

            // compare scores
            if (currentPlayer.getScore() > winner.getScore()) {
                winner = currentPlayer;

            } else if (currentPlayer.getScore() == winner.getScore()) {
                // tie breaker, player with most territory
                int currentTerritory = mapGrid.countTerritory(currentPlayer.getSymbol());
                int winnerTerritory = mapGrid.countTerritory(winner.getSymbol());
                if (currentTerritory > winnerTerritory) {
                    winner = currentPlayer;
                } else if (currentTerritory == winnerTerritory) {
                    // Tie (Territory + points)
                    return null;
                }
            }
        }

        return winner;
    }
}