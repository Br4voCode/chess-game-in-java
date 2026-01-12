package chess.model;

import java.util.List;
import chess.model.player.Player;
import chess.model.pieces.PieceColor;
import chess.model.history.StepHistory;

public class Game {
    private List<Player> players;
    private Board board;
    private StepHistory history;
    private PieceColor currentTurn; // Rastrear el turno actual

    public Game(List<Player> players, Board board) {
        this.players = players;
        this.board = board;
        this.history = new StepHistory();
        this.currentTurn = PieceColor.WHITE; // Las blancas comienzan
    }

    public Game(List<Player> players, Board board, StepHistory history) {
        this.players = players;
        this.board = board;
        this.history = history;
        this.currentTurn = PieceColor.WHITE;
    }

    public void startNewGame(List<Player> players) {
        this.players = players;
        board = new Board();
        board.initialize();
        this.currentTurn = PieceColor.WHITE;
    }

    public Board getBoard() {
        return board;
    }

    public List<Player> getPlayers() {
        return players;
    }

    /**
     * Obtiene el color del jugador actual (cuyo turno es)
     */
    public PieceColor getCurrentTurn() {
        return currentTurn;
    }

    /**
     * Cambia el turno al siguiente jugador
     */
    public void switchTurn() {
        currentTurn = currentTurn == PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE;
    }

}