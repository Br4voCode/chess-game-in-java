package chess.model;

import java.util.List;

public abstract class Piece {
    protected final PieceColor color;
    protected final PieceType type;

    public Piece(PieceColor color, PieceType type) {
        this.color = color;
        this.type = type;
    }

    public PieceColor getColor() { return color; }
    public PieceType getType() { return type; }

    /**
     * Generate pseudo-legal moves (may include moves leaving king in check).
     * Must be filtered by Board.generateLegalMoves().
     */
    public abstract List<Move> getPseudoLegalMoves(Board board, Position pos);

    public String toUnicode() {
        // simple unicode chess pieces
        if (color == PieceColor.WHITE) {
            switch (type) {
                case KING: return "\u2654";
                case QUEEN: return "\u2655";
                case ROOK: return "\u2656";
                case BISHOP: return "\u2657";
                case KNIGHT: return "\u2658";
                case PAWN: return "\u2659";
            }
        } else {
            switch (type) {
                case KING: return "\u265A";
                case QUEEN: return "\u265B";
                case ROOK: return "\u265C";
                case BISHOP: return "\u265D";
                case KNIGHT: return "\u265E";
                case PAWN: return "\u265F";
            }
        }
        return "?";
    }
}
