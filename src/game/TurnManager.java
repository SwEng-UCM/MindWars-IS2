package game;

import player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages turn order and enforces that only the active player can interact.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Track whose turn it is (human or bot).</li>
 *   <li>Advance to the next player with {@link #advanceTurn()}.</li>
 *   <li>Expose whether the current player is a bot via {@link #isCurrentPlayerBot()}.</li>
 *   <li>Guard interactions: {@link #isActivePlayer(Player)} returns {@code false}
 *       for anyone who is not the current player, so callers can reject out-of-turn
 *       actions without knowing the internal index.</li>
 *   <li>Notify registered {@link TurnChangeListener}s whenever the active player
 *       changes, so UI and game-loop code can react declaratively.</li>
 * </ul>
 */
public class TurnManager {

    // -----------------------------------------------------------------------
    // Observer contract
    // -----------------------------------------------------------------------

    /**
     * Callback fired whenever the active player changes.
     * Implement this interface to react to turn transitions (e.g., update UI,
     * trigger bot thinking, disable input fields for non-active players).
     */
    public interface TurnChangeListener {
        /**
         * Called after the turn has advanced.
         *
         * @param previousPlayer the player whose turn just ended
         * @param currentPlayer  the player whose turn has just begun
         * @param roundNumber    the current round number from {@link GameState}
         */
        void onTurnChanged(Player previousPlayer, Player currentPlayer, int roundNumber);
    }

    // -----------------------------------------------------------------------
    // Fields
    // -----------------------------------------------------------------------

    private final GameState state;
    private final List<TurnChangeListener> listeners = new ArrayList<>();

    // -----------------------------------------------------------------------
    // Construction
    // -----------------------------------------------------------------------

    public TurnManager(GameState state) {
        this.state = state;
    }

    // -----------------------------------------------------------------------
    // Core turn API
    // -----------------------------------------------------------------------

    /**
     * Returns the player whose turn it currently is.
     *
     * @throws IllegalStateException if there are no players in the game state
     */
    public Player getCurrentPlayer() {
        ensurePlayersPresent();
        return state.getPlayers().get(state.getCurrentPlayerIndex());
    }

    /**
     * Advances to the next player in round-robin order and notifies listeners.
     * If advancing causes the index to wrap back to 0, the round number in
     * {@link GameState} is incremented automatically.
     *
     * @throws IllegalStateException if there are no players in the game state
     */
    public void advanceTurn() {
        ensurePlayersPresent();

        Player previous = getCurrentPlayer();
        int size = state.getPlayers().size();
        int next = (state.getCurrentPlayerIndex() + 1) % size;

        // Wrap-around → new round
        if (next == 0) {
            state.setRoundNumber(state.getRoundNumber() + 1);
        }

        state.setCurrentPlayerIndex(next);
        Player current = getCurrentPlayer();

        notifyListeners(previous, current);
    }

    /**
     * Resets the turn order back to the first player (index 0) without
     * modifying the round counter or notifying listeners.
     * Use this when starting a fresh game after {@link GameState#reset()}.
     */
    public void resetTurn() {
        state.setCurrentPlayerIndex(0);
    }

    // -----------------------------------------------------------------------
    // Active-player guard
    // -----------------------------------------------------------------------

    /**
     * Returns {@code true} only if {@code player} is the currently active player.
     *
     * <p>Use this guard at any interaction entry-point to enforce turn order:
     * <pre>{@code
     *   if (!turnManager.isActivePlayer(interactingPlayer)) {
     *       io.println("It's not your turn!");
     *       return;
     *   }
     * }</pre>
     *
     * @param player the player attempting to interact; may be {@code null}
     * @return {@code true} if {@code player} is the current active player
     */
    public boolean isActivePlayer(Player player) {
        if (player == null) {
            return false;
        }
        return getCurrentPlayer() == player;
    }

    // -----------------------------------------------------------------------
    // Bot helpers
    // -----------------------------------------------------------------------

    /**
     * Returns {@code true} if the current player is a bot.
     * Callers can use this to decide whether to prompt for human input or
     * delegate to the bot's {@link bot.BotStrategy}.
     */
    public boolean isCurrentPlayerBot() {
        return getCurrentPlayer().isBot();
    }

    /**
     * Returns {@code true} if the player at the given index is a bot.
     *
     * @param index player index (0-based) within {@link GameState#getPlayers()}
     * @throws IndexOutOfBoundsException if {@code index} is out of range
     */
    public boolean isBotAtIndex(int index) {
        return state.getPlayers().get(index).isBot();
    }

    // -----------------------------------------------------------------------
    // Observer management
    // -----------------------------------------------------------------------

    /**
     * Registers a listener to be notified on every turn change.
     * Duplicate registrations are silently ignored.
     *
     * @param listener the listener to add; must not be {@code null}
     */
    public void addTurnChangeListener(TurnChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Removes a previously registered listener.
     * No-op if the listener was not registered.
     *
     * @param listener the listener to remove
     */
    public void removeTurnChangeListener(TurnChangeListener listener) {
        listeners.remove(listener);
    }

    // -----------------------------------------------------------------------
    // Convenience / informational
    // -----------------------------------------------------------------------

    /**
     * Returns the total number of players managed by this turn cycle.
     */
    public int getPlayerCount() {
        return state.getPlayers().size();
    }

    /**
     * Returns the current round number as tracked by {@link GameState}.
     */
    public int getRoundNumber() {
        return state.getRoundNumber();
    }

    /**
     * Returns the zero-based index of the currently active player.
     */
    public int getCurrentPlayerIndex() {
        return state.getCurrentPlayerIndex();
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private void ensurePlayersPresent() {
        if (state.getPlayers() == null || state.getPlayers().isEmpty()) {
            throw new IllegalStateException(
                    "TurnManager: GameState has no players. Add players before calling turn methods.");
        }
    }

    private void notifyListeners(Player previous, Player current) {
        for (TurnChangeListener listener : listeners) {
            listener.onTurnChanged(previous, current, state.getRoundNumber());
        }
    }
}