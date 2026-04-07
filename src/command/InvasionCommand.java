package command;

import model.GameModel;
import model.GamePhase;

/**
 * Reversible wrapper around {@link GameModel#resolveInvasion(String, String)}.
 * Snapshots the target cell's owner, the invader index and the current
 * phase before resolving the battle so {@link #undo()} can restore them.
 */
public class InvasionCommand implements Command {

    private final GameModel model;
    private final String attackerAnswer;
    private final String defenderAnswer;

    private char oldTargetOwner;
    private int oldInvaderIndex;
    private int oldAttackToRow;
    private int oldAttackToCol;
    private GamePhase oldPhase;

    public InvasionCommand(GameModel model, String attackerAnswer, String defenderAnswer) {
        this.model = model;
        this.attackerAnswer = attackerAnswer;
        this.defenderAnswer = defenderAnswer;
    }

    @Override
    public void execute() {
        oldInvaderIndex = model.getInvaderIndex();
        oldAttackToRow = model.getAttackToRow();
        oldAttackToCol = model.getAttackToCol();
        oldTargetOwner = model.getMap().getOwner(oldAttackToRow, oldAttackToCol);
        oldPhase = model.getPhase();

        model.resolveInvasion(attackerAnswer, defenderAnswer);
    }

    @Override
    public void undo() {
        model.getMap().setOwner(oldAttackToRow, oldAttackToCol,
                oldTargetOwner == 0 ? '.' : oldTargetOwner);
        model.setInvaderIndex(oldInvaderIndex);
        model.forcePhase(oldPhase);
    }

    @Override
    public String describe() {
        return "Invasion resolve";
    }
}
