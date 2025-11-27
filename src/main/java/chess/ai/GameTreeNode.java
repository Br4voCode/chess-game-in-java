package chess.ai;

import chess.model.Board;
import chess.model.Move;
import chess.model.PieceColor;

import java.util.ArrayList;
import java.util.List;

/**
 * Explicit node for a game tree. Stores a copy of Board at this node.
 */
public class GameTreeNode {
    private final Board board;
    private final Move moveFromParent;
    private final PieceColor sideToMove;
    private final GameTreeNode parent;
    private final List<GameTreeNode> children = new ArrayList<>();
    private final int depth;
    private Integer evaluation = null;
    private boolean expanded = false;

    public GameTreeNode(Board board, Move moveFromParent, PieceColor sideToMove, GameTreeNode parent, int depth) {
        this.board = board;
        this.moveFromParent = moveFromParent;
        this.sideToMove = sideToMove;
        this.parent = parent;
        this.depth = depth;
    }

    public Board getBoard() { return board; }
    public Move getMoveFromParent() { return moveFromParent; }
    public PieceColor getSideToMove() { return sideToMove; }
    public GameTreeNode getParent() { return parent; }
    public List<GameTreeNode> getChildren() { return children; }
    public int getDepth() { return depth; }
    public Integer getEvaluation() { return evaluation; }
    public void setEvaluation(int eval) { this.evaluation = eval; }
    public boolean isExpanded() { return expanded; }

    public void expand() {
        if (expanded) return;
        List<chess.model.Move> moves = board.getAllPossibleMoves(sideToMove);
        PieceColor next = sideToMove.opposite();
        for (chess.model.Move m : moves) {
            Board nb = board.copy();
            nb.movePiece(m);
            GameTreeNode child = new GameTreeNode(nb, m, next, this, this.depth + 1);
            children.add(child);
        }
        expanded = true;
    }

    @Override
    public String toString() {
        return "Node(depth=" + depth + ", children=" + children.size() + ", eval=" + evaluation + ")";
    }
}
