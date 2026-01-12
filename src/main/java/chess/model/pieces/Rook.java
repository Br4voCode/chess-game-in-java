package chess.model.pieces;

import chess.model.*;

import java.util.ArrayList;
import java.util.List;

import chess.model.pieces.PieceColor;
import chess.model.pieces.PieceType;

public class Rook extends Piece {
    private boolean hasMoved = false;

    public Rook(PieceColor color) {
        super(color, PieceType.ROOK);
    }

    public boolean hasMovedFromStart() {
        return hasMoved;
    }

    public void setHasMoved(boolean moved) {
        this.hasMoved = moved;
    }

    @Override
    public List<Move> getPseudoLegalMoves(Board board, Position pos) {
        List<Move> moves = new ArrayList<>();
        int[][] directions = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
        for (int[] d : directions) {
            int r = pos.getRow() + d[0];
            int c = pos.getCol() + d[1];
            while (r >= 0 && r < 8 && c >= 0 && c < 8) {
                Position to = new Position(r, c);
                Piece target = board.getPieceAt(to);
                if (target == null)
                    moves.add(new Move(pos, to));
                else {
                    if (target.getColor() != color)
                        moves.add(new Move(pos, to));
                    break;
                }
                r += d[0];
                c += d[1];
            }
        }
        return moves;
    }
}
