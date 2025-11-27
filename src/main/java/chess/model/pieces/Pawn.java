package chess.model.pieces;

import chess.model.*;

import java.util.ArrayList;
import java.util.List;

public class Pawn extends Piece {
    public Pawn(PieceColor color) { super(color, PieceType.PAWN); }

    @Override
    public List<Move> getPseudoLegalMoves(Board board, Position pos) {
        List<Move> moves = new ArrayList<>();
        int dir = (color == PieceColor.WHITE) ? -1 : 1; // white moves up (decreasing row)
        int startRow = (color == PieceColor.WHITE) ? 6 : 1;

        // forward one
        Position one = new Position(pos.getRow() + dir, pos.getCol());
        if (one.isValid() && board.getPieceAt(one) == null) {
            // promotion check handled at Board.applyMove
            moves.add(new Move(pos, one));
            // forward two from start
            Position two = new Position(pos.getRow() + 2*dir, pos.getCol());
            if (pos.getRow() == startRow && board.getPieceAt(two) == null) {
                moves.add(new Move(pos, two));
            }
        }

        // captures
        int[] dc = {-1, 1};
        for (int dcol : dc) {
            Position cap = new Position(pos.getRow() + dir, pos.getCol() + dcol);
            if (!cap.isValid()) continue;
            Piece target = board.getPieceAt(cap);
            if (target != null && target.getColor() != color) {
                moves.add(new Move(pos, cap));
            }
        }

        // Note: en-passant omitted in this simplified version
        return moves;
    }
}
