package chess.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import chess.model.Board;
import chess.model.Move;
import chess.model.PieceColor;
import chess.util.GameLogger;

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
    private final Random random = new Random();

    public MinimaxTreeSearch(GameTree tree, BoardEvaluator evaluator, PieceColor maximizingColor) {
        this.tree = tree;
        this.evaluator = evaluator;
        this.maximizingColor = maximizingColor;
    }

    /**
     * Run minimax and return best Move from root.
     */
    public Move runAndGetBestMove() {
        return runAndGetBestMove(tree.getRoot(), true);
    }

    private Move runAndGetBestMove(GameTreeNode root, boolean isRoot) {
        GameLogger logger = GameLogger.getInstance();

        propagate(root);

        List<GameTreeNode> bestNodes = new ArrayList<>();
        int best = Integer.MIN_VALUE;

        for (GameTreeNode child : root.getChildren()) {
            Integer s = child.getEvaluation();
            if (s == null)
                continue;

            if (child.getMoveFromParent() != null) {
                child.getMoveFromParent().setValue(s);
            }

            if (s > best) {
                best = s;
                bestNodes.clear();
                bestNodes.add(child);
            } else if (s == best) {
                bestNodes.add(child);
            }
        }

        if (bestNodes.isEmpty()) {
            logger.log("  → No hay movimientos candidatos.");
            return null;
        }

        if (bestNodes.size() > 1) {

            GameTreeNode bestNode = null;
            int bestDeepScore = Integer.MIN_VALUE;
            List<GameTreeNode> deepTiedNodes = new ArrayList<>();

            for (GameTreeNode tiedNode : bestNodes) {
                int deepScore = evaluateMoreDeeply(tiedNode, 1);

                if (deepScore > bestDeepScore) {
                    bestDeepScore = deepScore;
                    deepTiedNodes.clear();
                    deepTiedNodes.add(tiedNode);
                } else if (deepScore == bestDeepScore) {
                    deepTiedNodes.add(tiedNode);
                }
            }

            if (deepTiedNodes.size() == 1) {
                bestNode = deepTiedNodes.get(0);
            } else {
                bestNode = deepTiedNodes.get(random.nextInt(deepTiedNodes.size()));
            }

            Move bestMove = bestNode.getMoveFromParent();
            if (bestMove != null) {
                logger.log("  → Movimiento seleccionado tras desempate: " + bestMove + " (score: " + best + ")");
            }
            return bestMove;
        } else {
            Move bestMove = bestNodes.get(0).getMoveFromParent();
            if (bestMove != null && isRoot) {
                logger.log("  → Movimiento mejor evaluado: " + bestMove + " (score: " + best + ")");
            }
            return bestMove;
        }
    }

    private int evaluateMoreDeeply(GameTreeNode node, int additionalDepth) {
        if (additionalDepth <= 0 || node.getChildren().isEmpty()) {
            return node.getEvaluation() != null ? node.getEvaluation()
                    : evaluator.evaluate(node.getBoard(), maximizingColor);
        }

        int bestScore;
        if (node.getSideToMove() == maximizingColor) {
            bestScore = Integer.MAX_VALUE;
            for (GameTreeNode child : node.getChildren()) {
                int childEval = evaluateMoreDeeply(child, additionalDepth - 1);
                bestScore = Math.min(bestScore, childEval);
            }
        } else {
            bestScore = Integer.MIN_VALUE;
            for (GameTreeNode child : node.getChildren()) {
                int childEval = evaluateMoreDeeply(child, additionalDepth - 1);
                bestScore = Math.max(bestScore, childEval);
            }
        }

        return bestScore;
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
            if (nodeMax)
                best = Math.max(best, val);
            else
                best = Math.min(best, val);
        }
        node.setEvaluation(best);
        return best;
    }
}