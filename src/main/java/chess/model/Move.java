package chess.model;

import java.io.Serializable;

public class Move implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Position from;
    private final Position to;
    private final Piece promotion;
    private final boolean isEnPassant; // optional piece when pawn promotes (null means promote to queen by default)

    public Move(Position from, Position to) {
        this(from, to, null, false);
    }

    public Move(Position from, Position to, Piece promotion) {
        this(from, to, promotion, false);
    }

    public Move(Position from, Position to, Piece promotion, boolean isEnPassant) {
        this.from = from;
        this.to = to;
        this.promotion = promotion;
        this.isEnPassant = isEnPassant;
    }

    public Position getFrom() { return from; }
    public Position getTo() { return to; }
    public Piece getPromotion() { return promotion; }
    public boolean isEnPassant() { return isEnPassant; }

    @Override
    public String toString() {
        return from + "->" + to + (promotion != null ? "={" + promotion.getType() + "}" : "");
    }
}
