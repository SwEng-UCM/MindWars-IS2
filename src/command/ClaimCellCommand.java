package command;

import game.MapGrid;
import model.GameModel;

/**
 * Reversible wrapper around {@link GameModel#claimCell(int, int, int)}.
 * Snapshots the previous owner of the target cell so {@link #undo()} can
 * put it back (empty cells are represented by the '.' character).
 */
public class ClaimCellCommand implements Command {

    private final GameModel model;
    private final int playerIndex;
    private final int row;
    private final int col;

    private char oldOwner;
    private boolean executed;

    public ClaimCellCommand(GameModel model, int playerIndex, int row, int col) {
        this.model = model;
        this.playerIndex = playerIndex;
        this.row = row;
        this.col = col;
    }

    @Override
    public void execute() {
        MapGrid map = model.getMap();
        oldOwner = map.getOwner(row, col);
        executed = model.claimCell(playerIndex, row, col);
    }

    @Override
    public void undo() {
        if (!executed) return;
        model.getMap().setOwner(row, col, oldOwner == 0 ? '.' : oldOwner);
    }

    public boolean wasAccepted() {
        return executed;
    }

    @Override
    public String describe() {
        return "Claim (" + row + "," + col + ") by player " + playerIndex;
    }
}
