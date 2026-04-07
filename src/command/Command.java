package command;

/**
 * The Command pattern interface (addresses #81). Each player action
 * (answer submission, cell claim, invasion) is encapsulated as a concrete
 * Command that captures enough state to execute itself and, later, undo
 * itself. {@link CommandHistory} keeps a stack of executed commands and
 * drives the single-level undo feature (#82).
 */
public interface Command {

    /** Applies this command's effect to the model. */
    void execute();

    /**
     * Reverts this command's effect. Must only be called after
     * {@link #execute()}. After {@code undo()} returns, the model must be
     * in the state it was in before {@code execute()} was invoked.
     */
    void undo();

    /** Short description for debugging / UI tooltips. */
    String describe();
}
