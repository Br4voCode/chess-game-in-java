package chess.model.pieces;

import chess.model.*;

import java.util.ArrayList;
import java.util.List;

public class King extends Piece {
    public King(PieceColor color) { super(color, PieceType.KING); }

    @Override
    public List<Move> getPseudoLegalMoves(Board board, Position pos) {
        List<Move> moves = new ArrayList<>();
        int[][] deltas = {{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};
        for (int[] d : deltas) {
            int r = pos.getRow() + d[0];
            int c = pos.getCol() + d[1];
            Position to = new Position(r, c);
            if (!to.isValid()) continue;
            Piece target = board.getPieceAt(to);
            if (target == null || target.getColor() != color) {
                moves.add(new Move(pos, to));
            }
        }
        return moves;
    }
}
