package game;

import player.Player;
import java.util.ArrayList;
import java.util.List;

public class GameState {
    private List<Player> players;
    private int roundNumber;
    private int currentPlayerIndex;
    private int currentWager = 0;

    public GameState() {
        this.players = new ArrayList<>();
        this.roundNumber = 1;
        this.currentPlayerIndex = 0;
    }

    public void addPlayer(Player player) {
        this.players.add(player);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public void setCurrentPlayerIndex(int index) {
        this.currentPlayerIndex = index;
    }

    public void reset() {
        this.players.clear();
        this.roundNumber = 1;
        this.currentPlayerIndex = 0;
        this.currentWager = 0;
    }

    public void setCurrentWager(int wager) {
        this.currentWager = wager;
    }

    public int getCurrentWager() {
        return currentWager;
    }
}