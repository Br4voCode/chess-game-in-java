package chess.model;

public class Move {
    private final Position from;
    private final Position to;
    private final Piece promotion; // optional piece when pawn promotes (null means promote to queen by default)

    public Move(Position from, Position to) {
        this(from, to, null);
    }

    public Move(Position from, Position to, Piece promotion) {
        this.from = from;
        this.to = to;
        this.promotion = promotion;
    }

    public Position getFrom() { return from; }
    public Position getTo() { return to; }
    public Piece getPromotion() { return promotion; }

    @Override
    public String toString() {
        return from + "->" + to + (promotion != null ? "={" + promotion.getType() + "}" : "");
    }
}
