package chess.game;

import chess.model.Board;
import chess.model.GameClock;
import chess.model.Move;
import chess.model.Piece;
import chess.model.PieceColor;
import chess.history.Step;
import chess.history.StepHistory;
import chess.history.StepHistoryStore;
import chess.rules.RulesEngine;

public class Game {
    private Board board;
    private Player white;
    private Player black;
    private PieceColor turn;
    private boolean gameOver;
    private String gameResult;
    private int moveCount;
    private StepHistory stepHistory;
    private StepHistoryStore stepHistoryStore;
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
        this.stepHistory = new StepHistory();
        this.stepHistoryStore = new StepHistoryStore("game_history.dat");
        this.gameClock = new GameClock(); // Initialize with 5 minutes per player
    }

    public Board getBoard() {
        return board;
    }

    public PieceColor getTurn() {
        return turn;
    }

    /**
     * Internal setter used by history navigation (undo/redo).
     */
    public void setTurn(PieceColor turn) {
        this.turn = turn;
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

    /**
     * Internal setter used by history navigation (undo/redo).
     */
    public void setMoveCount(int moveCount) {
        this.moveCount = Math.max(0, moveCount);
    }

    /**
     * Clears game-over state when navigating history.
     */
    public void clearGameOverState() {
        this.gameOver = false;
        this.gameResult = null;
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

    // Capture board state needed for deterministic undo/redo
    chess.model.Position enPassantBefore = board.getEnPassantTarget();
    Piece moverBefore = board.getPieceAt(m.getFrom());

    // Apply the move and track captured piece
    lastCapturedPiece = board.movePiece(m);

    chess.model.Position enPassantAfter = board.getEnPassantTarget();
        turn = turn.opposite();
        moveCount++;

        // Update game clock - switch active player
        gameClock.switchPlayer();

        // Guardar el step en el historial (solo si está habilitado)
        if (shouldSaveMoves) {
            Step step = buildStepForAppliedMove(m, moverBefore, lastCapturedPiece, enPassantBefore, enPassantAfter);
            stepHistory.recordApplied(step);
            stepHistoryStore.saveApplied(stepHistory);
        }

        // Check game state after move
        checkGameState();

        return true;
    }

    private Step buildStepForAppliedMove(Move move,
            Piece moverBefore,
            Piece capturedPiece,
            chess.model.Position enPassantBefore,
            chess.model.Position enPassantAfter) {
        Piece moverAfter = board.getPieceAt(move.getTo());
        PieceColor moverColor = moverAfter != null ? moverAfter.getColor() : turn.opposite(); // best-effort
        chess.model.PieceType moverType = moverAfter != null ? moverAfter.getType() : null;

        // IMPORTANT: must match the existing UI format exactly
        // Examples: "e2-e4" or "O-O" / "O-O-O" (castling is handled by the controller UI,
        // but we keep generic notation here).
        String displayText = toChessNotation(move.getFrom()) + "-" + toChessNotation(move.getTo());

        // Special cases
        boolean castling = moverBefore != null && moverBefore.getType() == chess.model.PieceType.KING &&
                move.getFrom().getRow() == move.getTo().getRow() &&
                Math.abs(move.getFrom().getCol() - move.getTo().getCol()) == 2;

        chess.model.Position rookFrom = null;
        chess.model.Position rookTo = null;
        Boolean rookHadMovedBefore = null;
        if (castling) {
            int row = move.getFrom().getRow();
            if (move.getTo().getCol() > move.getFrom().getCol()) {
                rookFrom = new chess.model.Position(row, 7);
                rookTo = new chess.model.Position(row, 5);
            } else {
                rookFrom = new chess.model.Position(row, 0);
                rookTo = new chess.model.Position(row, 3);
            }
            Piece rookBefore = board.getPieceAt(rookTo); // after move, rook is already on rookTo
            if (rookBefore instanceof chess.model.pieces.Rook) {
                // We don't have rook "before" snapshot here; best effort is to assume false.
                // We'll compute the true "before" in controller when we build the step from MoveResult.
                rookHadMovedBefore = ((chess.model.pieces.Rook) rookBefore).hasMovedFromStart();
            }
        }

        boolean enPassant = moverBefore != null && moverBefore.getType() == chess.model.PieceType.PAWN &&
                enPassantBefore != null && move.getTo().equals(enPassantBefore) &&
                capturedPiece != null && capturedPiece.getType() == chess.model.PieceType.PAWN;
        chess.model.Position enPassantCapturedPawnPos = null;
        if (enPassant) {
            enPassantCapturedPawnPos = new chess.model.Position(
                    moverBefore.getColor() == PieceColor.WHITE ? move.getTo().getRow() + 1 : move.getTo().getRow() - 1,
                    move.getTo().getCol());
        }

        boolean promotion = moverBefore != null && moverBefore.getType() == chess.model.PieceType.PAWN &&
                moverAfter != null && moverAfter.getType() != chess.model.PieceType.PAWN;

        Piece promotedTo = promotion ? moverAfter : null;
        Piece originalPawn = promotion ? moverBefore : null;

        Boolean moverHadMovedBefore = null;
        if (moverBefore instanceof chess.model.pieces.King) {
            moverHadMovedBefore = ((chess.model.pieces.King) moverBefore).hasMovedFromStart();
        } else if (moverBefore instanceof chess.model.pieces.Rook) {
            moverHadMovedBefore = ((chess.model.pieces.Rook) moverBefore).hasMovedFromStart();
        }

        return new Step(
                move,
                moverColor,
                moverType,
                displayText,
                capturedPiece,
                castling,
                rookFrom,
                rookTo,
                rookHadMovedBefore,
                enPassant,
                enPassantCapturedPawnPos,
                promotion,
                promotedTo,
                originalPawn,
                moverHadMovedBefore,
                enPassantBefore,
                enPassantAfter);
    }

    private String toChessNotation(chess.model.Position pos) {
        char file = (char) ('a' + pos.getCol());
        int rank = 8 - pos.getRow();
        return "" + file + rank;
    }

    private boolean isMoveLegal(Move m) {
        // Centralized query for legal moves
        java.util.List<Move> legalMoves = RulesEngine.legalMoves(board, turn);

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
        // Centralized evaluation (keeps Game small; delegates to Board's existing rule methods)
        String result = RulesEngine.evaluateGameResult(board, turn);
        if (result != null) {
            gameOver = true;
            gameResult = result;
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
        stepHistory.clear();
        stepHistoryStore.saveApplied(stepHistory);
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
        return RulesEngine.isInCheck(board, color);
    }

    public boolean isCheckmate(PieceColor color) {
        return RulesEngine.isCheckmate(board, color);
    }

    public boolean isStalemate(PieceColor color) {
        return RulesEngine.isStalemate(board, color);
    }

    public boolean isInsufficientMaterial() {
        return board.isInsufficientMaterial();
    }

    public String getGameStatus() {
        if (gameOver) {
            return gameResult;
        }

        if (RulesEngine.isInCheck(board, turn)) {
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

    public StepHistory getStepHistory() {
        return stepHistory;
    }

    public StepHistoryStore getStepHistoryStore() {
        return stepHistoryStore;
    }

    /**
     * Establece la ruta del archivo de historial (debe llamarse antes de empezar la
     * partida)
     * 
     * @param filePath la ruta del archivo .dat
     */
    public void setMoveHistoryPath(String filePath) {
        this.stepHistoryStore = new StepHistoryStore(filePath);
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

    /**
     * Establece el estado final del juego manualmente
     * Útil para terminar una partida desde el código (ej: en partidas IA vs IA)
     *
     * @param isOver si el juego ha terminado
     * @param result el resultado del juego
     */
    public void setGameOver(boolean isOver, String result) {
        this.gameOver = isOver;
        this.gameResult = result;
        if (isOver) {
            stopClock();
        }
    }
}