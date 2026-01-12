package chess.model.player;

import chess.model.Board;
import chess.model.Move;
import chess.model.pieces.PieceColor;

public abstract class Player {
    protected PieceColor color;

    public Player(PieceColor color) {
        this.color = color;
    }

    public PieceColor getColor() {
        return color;
    }

    public abstract Move chooseMove(Board board);
}
