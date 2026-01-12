package chess.ai;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

import chess.model.Board;
import chess.model.PieceColor;
import chess.util.GameLogger;

public class GameTree {
    private final GameTreeNode root;

    public GameTree(Board board, PieceColor sideToMove) {
        this.root = new GameTreeNode(board.copy(), null, sideToMove, null, 0);
    }

    public GameTreeNode getRoot() { return root; }

    /**
     * BFS expansion to given plyDepth.
     */
    public void buildToDepth(int plyDepth) {
        buildToDepth(plyDepth, null);
    }

    public void buildToDepth(int plyDepth, Consumer<GameTreeNode> onNodeVisited) {
        if (plyDepth <= 0) return;
        
        GameLogger logger = GameLogger.getInstance();
        long startTime = System.currentTimeMillis();
        int[] nodeCount = {0};
        
        Deque<GameTreeNode> q = new ArrayDeque<>();
        q.add(root);
        while (!q.isEmpty()) {
            GameTreeNode n = q.poll();
            if (n.getDepth() >= plyDepth) continue;
            n.expand();
            nodeCount[0]++;
            
            if (onNodeVisited != null) {
                onNodeVisited.accept(n);
            }
            
            
            if (nodeCount[0] % 1000 == 0) {
                long elapsed = System.currentTimeMillis() - startTime;
                logger.log("  → Nivel " + n.getDepth() + ": " + nodeCount[0] + " nodos construidos (" + elapsed + "ms)");
            }
            
            for (GameTreeNode c : n.getChildren()) {
                if (c.getDepth() < plyDepth) q.add(c);
            }
        }
        
        long totalTime = System.currentTimeMillis() - startTime;
        logger.log("  → Total: " + nodeCount[0] + " nodos en " + totalTime + "ms");
    }

    public List<GameTreeNode> traversePreOrder() {
        List<GameTreeNode> out = new ArrayList<>();
        traverse(root, out);
        return out;
    }

    private void traverse(GameTreeNode node, List<GameTreeNode> acc) {
        acc.add(node);
        for (GameTreeNode c : node.getChildren()) traverse(c, acc);
    }
}
