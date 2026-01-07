package chess.rules;

import chess.model.Move;
import chess.model.Piece;
import chess.model.PieceColor;

/**
 * Small value object describing what happened after a move is applied.
 * Kept intentionally simple to avoid spreading game-rule logic across UI.
 */
public final class MoveResult {
    private final boolean moveApplied;
    private final Move move;
    private final PieceColor nextTurn;

    private final Piece capturedPiece;

    private final boolean check;
    private final boolean checkmate;
    private final boolean stalemate;
    private final boolean insufficientMaterial;
    private final boolean gameOver;
    private final String gameResult;

    public MoveResult(
            boolean moveApplied,
            Move move,
            PieceColor nextTurn,
            Piece capturedPiece,
            boolean check,
            boolean checkmate,
            boolean stalemate,
            boolean insufficientMaterial,
            boolean gameOver,
            String gameResult) {
        this.moveApplied = moveApplied;
        this.move = move;
        this.nextTurn = nextTurn;
        this.capturedPiece = capturedPiece;
        this.check = check;
        this.checkmate = checkmate;
        this.stalemate = stalemate;
        this.insufficientMaterial = insufficientMaterial;
        this.gameOver = gameOver;
        this.gameResult = gameResult;
    }

    public static MoveResult notApplied(Move move, PieceColor currentTurn) {
        return new MoveResult(false, move, currentTurn, null, false, false, false, false, false, null);
    }

    public boolean isMoveApplied() {
        return moveApplied;
    }

    public Move getMove() {
        return move;
    }

    public PieceColor getNextTurn() {
        return nextTurn;
    }

    public Piece getCapturedPiece() {
        return capturedPiece;
    }

    public boolean isCheck() {
        return check;
    }

    public boolean isCheckmate() {
        return checkmate;
    }

    public boolean isStalemate() {
        return stalemate;
    }

    public boolean isInsufficientMaterial() {
        return insufficientMaterial;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public String getGameResult() {
        return gameResult;
    }
}
