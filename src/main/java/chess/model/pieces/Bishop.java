package chess.model.pieces;

import chess.model.*;

import java.util.ArrayList;
import java.util.List;

public class Bishop extends Piece {
    public Bishop(PieceColor color) { super(color, PieceType.BISHOP); }

    @Override
    public List<Move> getPseudoLegalMoves(Board board, Position pos) {
        List<Move> moves = new ArrayList<>();
        int[][] directions = {{1,1},{1,-1},{-1,1},{-1,-1}};
        for (int[] d : directions) {
            int r = pos.getRow() + d[0];
            int c = pos.getCol() + d[1];
            while (r >= 0 && r < 8 && c >= 0 && c < 8) {
                Position to = new Position(r,c);
                Piece target = board.getPieceAt(to);
                if (target == null) moves.add(new Move(pos,to));
                else {
                    if (target.getColor() != color) moves.add(new Move(pos,to));
                    break;
                }
                r += d[0]; c += d[1];
            }
        }
        return moves;
    }
}
