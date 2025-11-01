package chess.model.pieces;

import chess.model.*;

import java.util.ArrayList;
import java.util.List;

public class Knight extends Piece {
    public Knight(PieceColor color) { super(color, PieceType.KNIGHT); }

    @Override
    public List<Move> getPseudoLegalMoves(Board board, Position pos) {
        List<Move> moves = new ArrayList<>();
        int[][] deltas = {{2,1},{1,2},{-1,2},{-2,1},{-2,-1},{-1,-2},{1,-2},{2,-1}};
        for (int[] d : deltas) {
            Position to = new Position(pos.getRow()+d[0], pos.getCol()+d[1]);
            if (!to.isValid()) continue;
            Piece target = board.getPieceAt(to);
            if (target == null || target.getColor() != color) moves.add(new Move(pos,to));
        }
        return moves;
    }
}
