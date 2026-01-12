package chess.model.player;

import chess.model.Board;
import chess.model.Move;
import chess.model.pieces.PieceColor;

public class HumanPlayer extends Player {

    public HumanPlayer(PieceColor color) {
        super(color);
    }

    @Override
    public Move chooseMove(Board board) {
        return null;
    }
}
