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
        // Devolver identificadores simples para las piezas
        if (color == PieceColor.WHITE) {
            switch (type) {
                case KING: return "k";
                case QUEEN: return "q";
                case ROOK: return "r";
                case BISHOP: return "b";
                case KNIGHT: return "n";
                case PAWN: return "p";
            }
        } else {
            switch (type) {
                case KING: return "K";
                case QUEEN: return "Q";
                case ROOK: return "R";
                case BISHOP: return "B";
                case KNIGHT: return "N";
                case PAWN: return "P";
            }
        }
        return "?";
    }
}
