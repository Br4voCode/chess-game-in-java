package chess.game;

import chess.model.Board;
import chess.model.Move;

public abstract class Player {
    public abstract Move chooseMove(Board board);
}
