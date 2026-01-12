package chess.model;

import java.io.Serializable;

public class Move implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Position from;
    private final Position to;
    private final Piece promotion;
    private int value = -1000;

    public Move(Position from, Position to) {
        this(from, to, null);
    }

    public Move(Position from, Position to, Piece promotion) {
        this.from = from;
        this.to = to;
        this.promotion = promotion;
    }

    public void setValue(int num) {
        this.value = num;
    }

    public int getValue() {
        return this.value;
    }

    public Position getFrom() {
        return from;
    }

    public Position getTo() {
        return to;
    }

    public Piece getPromotion() {
        return promotion;
    }

    @Override
    public String toString() {
        return from + "->" + to + (promotion != null ? "={" + promotion.getType() + "}" : "");
    }
}
