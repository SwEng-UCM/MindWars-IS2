package command;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * LIFO stack of executed {@link Command}s. The controller pushes onto
 * this whenever a reversible action is taken; the Undo button pops the
 * top command and calls {@link Command#undo()}. Single-level undo per
 * #82 is naturally supported by simply undoing once; the stack keeps
 * more entries in case we later expand it.
 */
public class CommandHistory {

    private final Deque<Command> stack = new ArrayDeque<>();

    /** Pushes an already-executed command onto the history. */
    public void push(Command c) {
        stack.push(c);
    }

    /** Whether there is any command available to undo. */
    public boolean canUndo() {
        return !stack.isEmpty();
    }

    /**
     * Undoes and removes the most recently executed command. Returns
     * {@code true} if a command was undone, {@code false} if the stack
     * was empty.
     */
    public boolean undo() {
        if (stack.isEmpty())
            return false;
        Command c = stack.pop();
        c.undo();
        return true;
    }

    /** Empties the history. Called when a new game starts. */
    public void clear() {
        stack.clear();
    }

    public int size() {
        return stack.size();
    }
}
