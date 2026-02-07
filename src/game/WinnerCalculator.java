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
        // @TODO
        return null;
    }
}