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

    public static Player getWinnerOrNull(List<Player> players) {
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
                // if scores are tied, check the timer (lower is better)
                if (currentPlayer.getTimer() < winner.getTimer()) {
                    winner = currentPlayer;
                } else if (currentPlayer.getTimer() == winner.getTimer()) {
                    // if both score and timer are the same, it's a tie
                    return null;
                }
            }
        }
        
        return winner;
    }
}