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
        int promotionRow = (color == PieceColor.WHITE) ? 0 : 7;

        // forward one
        Position one = new Position(pos.getRow() + dir, pos.getCol());
        if (one.isValid() && board.getPieceAt(one) == null) {
            if (one.getRow() == promotionRow) {
                // Add promotion moves for all possible pieces
                moves.add(new Move(pos, one, new Queen(color)));
                moves.add(new Move(pos, one, new Rook(color)));
                moves.add(new Move(pos, one, new Bishop(color)));
                moves.add(new Move(pos, one, new Knight(color)));
            } else {
                moves.add(new Move(pos, one));
                // forward two from start
                Position two = new Position(pos.getRow() + 2*dir, pos.getCol());
                if (pos.getRow() == startRow && board.getPieceAt(two) == null) {
                    moves.add(new Move(pos, two));
                }
            }
        }

        // captures
        int[] dc = {-1, 1};
        for (int dcol : dc) {
            Position cap = new Position(pos.getRow() + dir, pos.getCol() + dcol);
            if (!cap.isValid()) continue;
            Piece target = board.getPieceAt(cap);
            if (target != null && target.getColor() != color) {
                if (cap.getRow() == promotionRow) {
                    // Add promotion captures for all possible pieces
                    moves.add(new Move(pos, cap, new Queen(color)));
                    moves.add(new Move(pos, cap, new Rook(color)));
                    moves.add(new Move(pos, cap, new Bishop(color)));
                    moves.add(new Move(pos, cap, new Knight(color)));
                } else {
                    moves.add(new Move(pos, cap));
                }
            }
        }

        // en passant captures
        Position enPassantTarget = board.getEnPassantTarget();
        if (enPassantTarget != null) {
            // Check if pawn can capture en passant
            int expectedRow = pos.getRow() + dir;
            int expectedColDiff = Math.abs(enPassantTarget.getCol() - pos.getCol());
            
            if (enPassantTarget.getRow() == expectedRow && expectedColDiff == 1) {
                // This is a valid en passant capture
                if (enPassantTarget.getRow() == promotionRow) {
                    // Add promotion en passant captures for all possible pieces
                    moves.add(new Move(pos, enPassantTarget, new Queen(color)));
                    moves.add(new Move(pos, enPassantTarget, new Rook(color)));
                    moves.add(new Move(pos, enPassantTarget, new Bishop(color)));
                    moves.add(new Move(pos, enPassantTarget, new Knight(color)));
                } else {
                    moves.add(new Move(pos, enPassantTarget));
                }
            }
        }

        return moves;
    }
}
