package chess.game;

import chess.model.Board;
import chess.model.PieceColor;
import chess.model.Move;

public class Game {
    private Board board;
    private Player white;
    private Player black;
    private PieceColor turn;

    public Game(Player white, Player black) {
        this.board = new Board();
        this.white = white;
        this.black = black;
        this.turn = PieceColor.WHITE;
    }

    public Board getBoard() { return board; }
    public PieceColor getTurn() { return turn; }

    public boolean applyMove(Move m) {
        // assumes m is legal
        board.movePiece(m);
        turn = turn.opposite();
        return true;
    }

    public Move getAIMoveIfAny() {
        Player p = (turn == PieceColor.WHITE) ? white : black;
        if (p instanceof AIPlayer) {
            return p.chooseMove(board);
        }
        return null;
    }
}
