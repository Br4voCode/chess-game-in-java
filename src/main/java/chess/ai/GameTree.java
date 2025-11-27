package chess.ai;

import chess.model.Board;
import chess.model.PieceColor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

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
        if (plyDepth <= 0) return;
        Deque<GameTreeNode> q = new ArrayDeque<>();
        q.add(root);
        while (!q.isEmpty()) {
            GameTreeNode n = q.poll();
            if (n.getDepth() >= plyDepth) continue;
            n.expand();
            for (GameTreeNode c : n.getChildren()) {
                if (c.getDepth() < plyDepth) q.add(c);
            }
        }
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
