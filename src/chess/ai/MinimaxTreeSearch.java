package chess.ai;

import chess.model.Board;
import chess.model.PieceColor;
import chess.model.Move;

import java.util.List;

/**
 * Minimax executed over an explicit GameTree.
 */
public class MinimaxTreeSearch {

    public interface BoardEvaluator {
        int evaluate(Board board, PieceColor perspective);
    }

    private final GameTree tree;
    private final BoardEvaluator evaluator;
    private final PieceColor maximizingColor;

    public MinimaxTreeSearch(GameTree tree, BoardEvaluator evaluator, PieceColor maximizingColor) {
        this.tree = tree;
        this.evaluator = evaluator;
        this.maximizingColor = maximizingColor;
    }

    /**
     * Run minimax and return best Move from root.
     */
    public Move runAndGetBestMove() {
        GameTreeNode root = tree.getRoot();
        propagate(root);
        // choose best child
        int best = Integer.MIN_VALUE;
        GameTreeNode bestNode = null;
        for (GameTreeNode child : root.getChildren()) {
            Integer s = child.getEvaluation();
            if (s == null) continue;
            if (s > best) {
                best = s;
                bestNode = child;
            }
        }
        return bestNode == null ? null : bestNode.getMoveFromParent();
    }

    private int propagate(GameTreeNode node) {
        List<GameTreeNode> children = node.getChildren();
        if (children.isEmpty()) {
            int val = evaluator.evaluate(node.getBoard(), maximizingColor);
            node.setEvaluation(val);
            return val;
        }
        boolean nodeMax = (node.getSideToMove() == maximizingColor);
        int best = nodeMax ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (GameTreeNode c : children) {
            int val = propagate(c);
            if (nodeMax) best = Math.max(best, val);
            else best = Math.min(best, val);
        }
        node.setEvaluation(best);
        return best;
    }
}
