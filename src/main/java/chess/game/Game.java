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
    private boolean shouldSaveMoves = true;
    private Piece lastCapturedPiece = null;
    private GameClock gameClock;
    private java.util.Map<String, Integer> positionHistory = new java.util.HashMap<>();

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
    // 5 minutos para cada jugador
    this.gameClock = new GameClock(5 * 60);
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

        if (gameOver) {
            return false;
        }

        if (gameClock.hasTimeExpired(turn)) {
            gameOver = true;
            PieceColor winner = (turn == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;
            gameResult = winner + " wins! " + turn + " ran out of time!";
            gameClock.stop();
            return false;
        }

        Piece movingPiece = board.getPieceAt(m.getFrom());
        if (movingPiece == null || movingPiece.getColor() != turn) {
            return false;
        }

        if (!isMoveLegal(m)) {
            return false;
        }

        chess.model.Position enPassantBefore = board.getEnPassantTarget();
        Piece moverBefore = board.getPieceAt(m.getFrom());

        lastCapturedPiece = board.movePiece(m);

        chess.model.Position enPassantAfter = board.getEnPassantTarget();
        turn = turn.opposite();
        moveCount++;

        String positionKey = generatePositionKey();
        positionHistory.put(positionKey, positionHistory.getOrDefault(positionKey, 0) + 1);

        gameClock.switchPlayer();

        if (shouldSaveMoves) {
            Step step = buildStepForAppliedMove(m, moverBefore, lastCapturedPiece, enPassantBefore, enPassantAfter);
            stepHistory.recordApplied(step);
            
            // Update metadata with current timer state before saving
            if (stepHistoryStore.getGameMetadata() != null) {
                chess.history.GameMetadata.GameMode mode = stepHistoryStore.getGameMetadata().getGameMode();
                long whiteTime = gameClock.getWhiteTimeRemainingMillis();
                long blackTime = gameClock.getBlackTimeRemainingMillis();
                stepHistoryStore.setGameMetadata(new chess.history.GameMetadata(mode, whiteTime, blackTime));
            }
            
            stepHistoryStore.saveApplied(stepHistory);
        }

        checkGameState();

        return true;
    }

    private Step buildStepForAppliedMove(Move move,
            Piece moverBefore,
            Piece capturedPiece,
            chess.model.Position enPassantBefore,
            chess.model.Position enPassantAfter) {
        Piece moverAfter = board.getPieceAt(move.getTo());
        PieceColor moverColor = moverAfter != null ? moverAfter.getColor() : turn.opposite();
        chess.model.PieceType moverType = moverAfter != null ? moverAfter.getType() : null;

        String displayText = toChessNotation(move.getFrom()) + "-" + toChessNotation(move.getTo());

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
            Piece rookBefore = board.getPieceAt(rookTo);
            if (rookBefore instanceof chess.model.pieces.Rook) {

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

        java.util.List<Move> legalMoves = RulesEngine.legalMoves(board, turn);

        for (Move legalMove : legalMoves) {
            if (legalMove.getFrom().equals(m.getFrom()) &&
                    legalMove.getTo().equals(m.getTo())) {
                return true;
            }
        }
        return false;
    }

    private void checkGameState() {

        String result = RulesEngine.evaluateGameResult(board, turn, this);
        if (result != null) {
            gameOver = true;
            gameResult = result;
        }

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
        gameClock.reset();

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
        gameClock.reset();
        stepHistory.clear();
        stepHistoryStore.saveApplied(stepHistory);
        positionHistory.clear();
    }

    public boolean undoLastMove() {
        if (moveCount == 0) {
            return false;
        }

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
     * Establece el modo de juego en los metadatos
     * 
     * @param gameMode el modo de juego (PVP, PVAI, AIVAI)
     */
    public void setGameMode(chess.history.GameMetadata.GameMode gameMode) {
        long whiteTime = gameClock.getWhiteTimeRemainingMillis();
        long blackTime = gameClock.getBlackTimeRemainingMillis();
        chess.history.GameMetadata metadata = new chess.history.GameMetadata(gameMode, whiteTime, blackTime);
        this.stepHistoryStore.setGameMetadata(metadata);
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

    /**
     * Generate a unique key representing the current board position.
     * Used for threefold repetition detection.
     */
    private String generatePositionKey() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece p = board.getPieceAt(new chess.model.Position(row, col));
                if (p == null) {
                    sb.append('.');
                } else {
                    char c = p.getType().toString().charAt(0);
                    sb.append(p.getColor() == PieceColor.WHITE ? Character.toUpperCase(c) : Character.toLowerCase(c));
                }
            }
        }
        sb.append('_').append(turn);
        return sb.toString();
    }

    /**
     * Check if the current position has occurred three times (threefold repetition).
     */
    public boolean hasThreefoldRepetition() {
        String currentPosition = generatePositionKey();
        return positionHistory.getOrDefault(currentPosition, 0) >= 3;
    }

    /**
     * Get position history for debugging or analysis.
     */
    public java.util.Map<String, Integer> getPositionHistory() {
        return new java.util.HashMap<>(positionHistory);
    }
}