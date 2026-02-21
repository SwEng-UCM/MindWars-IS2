package game;

import player.Player;

//import java.util.List;

/**
 * PURPOSE:
 * - Handles turn order (“whose turn is it?”) so GameEngine stays readable.
 * - Encapsulates the logic for advancing to the next player.
 *
 * @TODO (MVP):
 *       - getCurrentPlayer(): return state.players[state.currentPlayerIndex]
 *       - advanceTurn(): increment index and wrap around (mod player count)
 *
 * @TODO (optional later):
 *       - Methods for special rounds, steal-turn mechanics, etc.
 */
public class TurnManager {
    private final GameState state;

    public TurnManager(GameState state) {
        this.state = state;
    }

    public Player getCurrentPlayer() {
        return state.getPlayers().get(state.getCurrentPlayerIndex());
    }

    public void advanceTurn() {
        int next = (state.getCurrentPlayerIndex() + 1) % state.getPlayers().size();
        state.setCurrentPlayerIndex(next);
    }
}