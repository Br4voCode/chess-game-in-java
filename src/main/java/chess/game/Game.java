package chess.game;

import chess.model.Board;
import chess.model.GameClock;
import chess.model.Move;
import chess.model.MoveHistory;
import chess.model.Piece;
import chess.model.PieceColor;

public class Game {
    private Board board;
    private Player white;
    private Player black;
    private PieceColor turn;
    private boolean gameOver;
    private String gameResult;
    private int moveCount;
    private MoveHistory moveHistory;
    private boolean shouldSaveMoves = true; // Flag para controlar si guardar movimientos
    private Piece lastCapturedPiece = null; // Track the last captured piece
    private GameClock gameClock; // Clock for tracking player time

    public Game(Player white, Player black) {
        this.board = new Board();
        this.white = white;
        this.black = black;
        this.turn = PieceColor.WHITE;
        this.gameOver = false;
        this.gameResult = null;
        this.moveCount = 0;
        this.moveHistory = new MoveHistory("game_history.dat");
        this.gameClock = new GameClock(); // Initialize with 5 minutes per player
    }

    public Board getBoard() {
        return board;
    }

    public PieceColor getTurn() {
        return turn;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public String getGameResult() {
        return gameResult;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public boolean applyMove(Move m) {
        // Check if game is over
        if (gameOver) {
            return false;
        }

        // Check if time has expired for current player
        if (gameClock.hasTimeExpired(turn)) {
            gameOver = true;
            PieceColor winner = (turn == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;
            gameResult = winner + " wins! " + turn + " ran out of time!";
            gameClock.stop();
            return false;
        }

        // Check if it's the correct player's turn
        Piece movingPiece = board.getPieceAt(m.getFrom());
        if (movingPiece == null || movingPiece.getColor() != turn) {
            return false;
        }

        // Check if move is legal
        if (!isMoveLegal(m)) {
            return false;
        }

        // Apply the move and track captured piece
        lastCapturedPiece = board.movePiece(m);
        // Apply the move
        board.movePiece(m);
        turn = turn.opposite();
        moveCount++;

        // Update game clock - switch active player
        gameClock.switchPlayer();

        // Guardar el movimiento en el historial (solo si está habilitado)
        if (shouldSaveMoves) {
            moveHistory.addMove(m);
        }

        // Check game state after move
        checkGameState();

        return true;
    }

    private boolean isMoveLegal(Move m) {
        // Get all legal moves for current player
        java.util.List<Move> legalMoves = board.getAllPossibleMoves(turn);

        // Check if the move is in the list of legal moves
        for (Move legalMove : legalMoves) {
            if (legalMove.getFrom().equals(m.getFrom()) &&
                    legalMove.getTo().equals(m.getTo())) {
                return true;
            }
        }
        return false;
    }

    private void checkGameState() {
        // Check for checkmate
        if (board.isCheckmate(turn)) {
            gameOver = true;
            PieceColor winner = (turn == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;
            gameResult = "Checkmate! " + winner + " wins!";
            return;
        }

        // Check for stalemate
        if (board.isStalemate(turn)) {
            gameOver = true;
            gameResult = "Stalemate! Game drawn.";
            return;
        }

        // Check for insufficient material
        if (board.isInsufficientMaterial()) {
            gameOver = true;
            gameResult = "Draw by insufficient material.";
            return;
        }

        // Optional: Check for 50-move rule
        // Optional: Check for threefold repetition
    }

    public Move getAIMoveIfAny() {
        if (gameOver) {
            return null;
        }

        Player currentPlayer = (turn == PieceColor.WHITE) ? white : black;
        if (currentPlayer instanceof AIPlayer) {
            return currentPlayer.chooseMove(board);
        }
        return null;
    }

    public Move getBestMove() {
        Player currentPlayer = (turn == PieceColor.WHITE) ? white : black;
        return currentPlayer.chooseMove(board);
    }

    public void reset() {
        board.initialize();
        turn = PieceColor.WHITE;
        gameOver = false;
        gameResult = null;
        moveCount = 0;
        gameClock.reset(); // Reset the clock
        // NO limpiar el historial aquí - solo resets internos
    }

    /**
     * Reinicia el juego para una nueva partida, limpiando el historial de
     * movimientos
     */
    public void resetForNewGame() {
        board.initialize();
        turn = PieceColor.WHITE;
        gameOver = false;
        gameResult = null;
        moveCount = 0;
        gameClock.reset(); // Reset the clock
        moveHistory.clear();
    }

    public boolean undoLastMove() {
        if (moveCount == 0) {
            return false;
        }

        // Note: This is a simplified undo. For a proper implementation,
        // you'd need to maintain a move history in the Board class.
        boolean success = board.undoLastMove();
        if (success) {
            turn = turn.opposite();
            moveCount--;
            gameOver = false;
            gameResult = null;
        }
        return success;
    }

    public boolean isKingInCheck(PieceColor color) {
        return board.isKingInCheck(color);
    }

    public boolean isCheckmate(PieceColor color) {
        return board.isCheckmate(color);
    }

    public boolean isStalemate(PieceColor color) {
        return board.isStalemate(color);
    }

    public boolean isInsufficientMaterial() {
        return board.isInsufficientMaterial();
    }

    public String getGameStatus() {
        if (gameOver) {
            return gameResult;
        }

        if (board.isKingInCheck(turn)) {
            return turn + " is in check";
        }

        return turn + " to move";
    }

    public Player getWhitePlayer() {
        return white;
    }

    public Player getBlackPlayer() {
        return black;
    }

    public void setPlayers(Player white, Player black) {
        this.white = white;
        this.black = black;
    }

    public Game copy() {
        Game copy = new Game(white, black);
        copy.board = this.board.copy();
        copy.turn = this.turn;
        copy.gameOver = this.gameOver;
        copy.gameResult = this.gameResult;
        copy.moveCount = this.moveCount;
        return copy;
    }

    /**
     * Obtiene el historial de movimientos de la partida
     * 
     * @return el objeto MoveHistory
     */
    public MoveHistory getMoveHistory() {
        return moveHistory;
    }

    /**
     * Establece la ruta del archivo de historial (debe llamarse antes de empezar la
     * partida)
     * 
     * @param filePath la ruta del archivo .dat
     */
    public void setMoveHistoryPath(String filePath) {
        this.moveHistory = new MoveHistory(filePath);
    }

    /**
     * Establece si los movimientos deben guardarse en el historial
     * 
     * @param shouldSave true para guardar movimientos, false para solo aplicarlos
     */
    public void setShouldSaveMoves(boolean shouldSave) {
        this.shouldSaveMoves = shouldSave;
    }

    /**
     * Obtiene la última pieza capturada
     * 
     * @return la última pieza capturada, o null si no hubo captura
     */
    public Piece getLastCapturedPiece() {
        return lastCapturedPiece;
    }

    /**
     * Limpia la última pieza capturada
     */
    public void clearLastCapturedPiece() {
        lastCapturedPiece = null;
    }

    /**
     * Get the game clock
     * 
     * @return the GameClock instance
     */
    public GameClock getGameClock() {
        return gameClock;
    }

    /**
     * Set a custom game clock
     * 
     * @param gameClock the GameClock to use
     */
    public void setGameClock(GameClock gameClock) {
        this.gameClock = gameClock;
    }

    /**
     * Start the game clock
     */
    public void startClock() {
        gameClock.start();
    }

    /**
     * Stop the game clock
     */
    public void stopClock() {
        gameClock.stop();
    }

    /**
     * Pause the game clock
     */
    public void pauseClock() {
        gameClock.pause();
    }

    /**
     * Resume the game clock
     */
    public void resumeClock() {
        gameClock.resume();
    }
}