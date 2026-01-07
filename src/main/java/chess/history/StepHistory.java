package chess.history;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * A double-ended queue based history with undo/redo.
 *
 * <p>
 * Semantics:
 * <ul>
 *   <li>"past" contains applied steps in order</li>
 *   <li>"future" contains steps that were undone and can be redone</li>
 *   <li>Recording a new step clears future</li>
 * </ul>
 */
public class StepHistory {
    private final Deque<Step> past = new ArrayDeque<>();
    private final Deque<Step> future = new ArrayDeque<>();

    public void clear() {
        past.clear();
        future.clear();
    }

    public int getAppliedCount() {
        return past.size();
    }

    public boolean canUndo() {
        return !past.isEmpty();
    }

    public boolean canRedo() {
        return !future.isEmpty();
    }

    /**
     * Records a new applied step. This clears redo history.
     */
    public void recordApplied(Step step) {
        if (step == null) {
            return;
        }
        past.addLast(step);
        future.clear();
    }

    /**
     * Pops an applied step to be undone, pushing it into future.
     */
    public Step popForUndo() {
        if (past.isEmpty()) {
            return null;
        }
        Step step = past.removeLast();
        future.addLast(step);
        return step;
    }

    /**
     * Pops a future step to be re-done, pushing it back into past.
     */
    public Step popForRedo() {
        if (future.isEmpty()) {
            return null;
        }
        Step step = future.removeLast();
        past.addLast(step);
        return step;
    }

    /**
     * Returns applied steps in order. (Future steps are not persisted by design.)
     */
    public List<Step> getAppliedSteps() {
        return Collections.unmodifiableList(new ArrayList<>(past));
    }

    public void loadAppliedSteps(List<Step> stepsInOrder) {
        clear();
        if (stepsInOrder == null) {
            return;
        }
        for (Step s : stepsInOrder) {
            past.addLast(s);
        }
    }
}
