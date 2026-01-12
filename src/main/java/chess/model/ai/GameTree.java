package chess.model.ai;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

import chess.model.pieces.PieceColor;
import chess.model.Board;

public class GameTree {
    private final GameTreeNode root;

    public GameTree(Board board, PieceColor sideToMove) {
        this.root = new GameTreeNode(board.copy(), null, sideToMove, null, 0);
    }

    public GameTreeNode getRoot() {
        return root;
    }

    /**
     * BFS expansion to given plyDepth.
     */
    public void buildToDepth(int plyDepth) {
        buildToDepth(plyDepth, null);
    }

    public void buildToDepth(int plyDepth, Consumer<GameTreeNode> onNodeVisited) {
        if (plyDepth <= 0)
            return;

        int[] nodeCount = { 0 };
        long startTime = System.currentTimeMillis();

        Deque<GameTreeNode> q = new ArrayDeque<>();
        q.add(root);
        while (!q.isEmpty()) {
            GameTreeNode n = q.poll();
            if (n.getDepth() >= plyDepth)
                continue;
            n.expand();
            nodeCount[0]++;

            if (onNodeVisited != null) {
                onNodeVisited.accept(n);
            }

            // Log progress every 1000 nodes
            if (nodeCount[0] % 1000 == 0) {
                long elapsed = System.currentTimeMillis() - startTime;
                System.out.println(
                        " → Nivel " + n.getDepth() + ": " + nodeCount[0] + " nodos construidos (" + elapsed + "ms)");
            }

            for (GameTreeNode c : n.getChildren()) {
                if (c.getDepth() < plyDepth)
                    q.add(c);
            }
        }

        long totalTime = System.currentTimeMillis() - startTime;
        System.out.println(" → Total: " + nodeCount[0] + " nodos en " + totalTime + "ms");
    }

    public List<GameTreeNode> traversePreOrder() {
        List<GameTreeNode> out = new ArrayList<>();
        traverse(root, out);
        return out;
    }

    private void traverse(GameTreeNode node, List<GameTreeNode> acc) {
        acc.add(node);
        for (GameTreeNode c : node.getChildren())
            traverse(c, acc);
    }
}
