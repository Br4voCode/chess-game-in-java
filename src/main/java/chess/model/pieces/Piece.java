package chess.model.pieces;

import chess.model.*;
import java.io.Serializable;
import java.util.List;
import chess.model.pieces.PieceColor;
import chess.model.pieces.PieceType;

public abstract class Piece implements Serializable {
    private static final long serialVersionUID = 1L;
    protected final PieceColor color;
    protected final PieceType type;

    public Piece(PieceColor color, PieceType type) {
        this.color = color;
        this.type = type;
    }

    public PieceColor getColor() {
        return color;
    }

    public PieceType getType() {
        return type;
    }

    /**
     * Generate pseudo-legal moves (may include moves leaving king in check).
     * Must be filtered by Board.generateLegalMoves().
     */
    public abstract List<Move> getPseudoLegalMoves(Board board, Position pos);

    public String toUnicode() {

        if (color == PieceColor.WHITE) {
            switch (type) {
                case KING:
                    return "♔";
                case QUEEN:
                    return "♕";
                case ROOK:
                    return "♖";
                case BISHOP:
                    return "♗";
                case KNIGHT:
                    return "♘";
                case PAWN:
                    return "♙";
            }
        } else {
            switch (type) {
                case KING:
                    return "♚";
                case QUEEN:
                    return "♛";
                case ROOK:
                    return "♜";
                case BISHOP:
                    return "♝";
                case KNIGHT:
                    return "♞";
                case PAWN:
                    return "♟";
            }
        }
        return "?";
    }
}
