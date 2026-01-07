package chess.rules;

import java.util.List;

import chess.game.Game;
import chess.model.Board;
import chess.model.Move;
import chess.model.Piece;
import chess.model.PieceColor;
import chess.view.GameView;

/**
 * Centralized, minimal rules layer.
 *
 * Pieces generate pseudo-legal moves; Board filters legal moves; Game keeps
 * turn/time/history.
 * This class offers a single entry point so UI/AI doesn't need to scatter
 * rules.
 */
public final class RulesEngine {
    private RulesEngine() {
    }

    public static List<Move> legalMoves(Board board, PieceColor color) {
        return board.getAllPossibleMoves(color);
    }

    /**
     * Convenience wrapper to map UI mode flags to a compact numeric game-mode code.
     *
     * <p>
     * This method does not inspect the {@link Game} instance. It only maps the two boolean flags
     * that the UI uses when creating a new game.
     *
     * <h3>Return value</h3>
     * Returns an {@code int} in the range {@code [1..3]}:
     * <ul>
     *   <li>{@code 1} = Player vs Player (PVP)</li>
     *   <li>{@code 2} = Player vs AI (PVAI)</li>
     *   <li>{@code 3} = AI vs AI (AIVAI)</li>
     * </ul>
     *
     * <h3>Parameters</h3>
     * <ul>
     *   <li>{@code isPlayerVsPlayer}: {@code true} when the game should run in PVP mode.</li>
     *   <li>{@code isAIVsAI}: {@code true} when the game should run in AI-vs-AI mode.</li>
     * </ul>
     *
     * <p>
     * Note: if {@code isAIVsAI} is {@code true}, it takes precedence and the result is {@code 3}.
     */
    public static int getGameModeNumber(boolean isPlayerVsPlayer, boolean isAIVsAI) {
        return GameView.getGameModeNumber(isPlayerVsPlayer, isAIVsAI);
    }

    /**
     * Creates a {@link MoveResult} snapshot from the current game state.
     * <p>
     * This is useful for modes where moves may be applied without going through
     * {@link #applyMove(chess.game.Game, chess.model.Move)} (e.g. AI vs AI match
     * driver), but the UI still needs a unified representation of terminal state.
     */
    public static MoveResult currentGameState(chess.game.Game game) {
        if (game == null) {
            return MoveResult.notApplied(null, null);
        }

        chess.model.Board board = game.getBoard();
        chess.model.PieceColor sideToMove = game.getTurn();

        boolean check = isInCheck(board, sideToMove);
        boolean checkmate = isCheckmate(board, sideToMove);
        boolean stalemate = isStalemate(board, sideToMove);
        boolean insufficientMaterial = board.isInsufficientMaterial();
        boolean threefoldRepetition = game.hasThreefoldRepetition();

        boolean gameOver = checkmate || stalemate || insufficientMaterial || threefoldRepetition;

        String gameResult;
        if (gameOver) {
            gameResult = evaluateGameResult(board, sideToMove, game);
        } else {
            gameResult = game.getGameResult();
        }

        return new MoveResult(
                true,
                null,
                sideToMove,
                null,
                check,
                checkmate,
                stalemate,
                insufficientMaterial,
                threefoldRepetition,
                gameOver,
                gameResult);
    }

    public static boolean isInCheck(Board board, PieceColor color) {
        return board.isKingInCheck(color);
    }

    public static boolean isCheckmate(Board board, PieceColor color) {
        return board.isCheckmate(color);
    }

    public static boolean isStalemate(Board board, PieceColor color) {
        return board.isStalemate(color);
    }

    /**
     * Returns a human-readable game result message if the side-to-move is already
     * in a terminal state.
     * Returns null if the game should continue.
     */
    public static String evaluateGameResult(Board board, PieceColor sideToMove) {
        if (board.isCheckmate(sideToMove)) {
            PieceColor winner = (sideToMove == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;
            return "Checkmate! " + winner + " wins!";
        }

        if (board.isStalemate(sideToMove)) {
            return "Stalemate! Game drawn.";
        }

        if (board.isInsufficientMaterial()) {
            return "Draw by insufficient material.";
        }

        return null;
    }

    /**
     * Returns a human-readable game result message including threefold repetition check.
     * Requires the Game instance to check position history.
     * Returns null if the game should continue.
     */
    public static String evaluateGameResult(Board board, PieceColor sideToMove, Game game) {
        if (board.isCheckmate(sideToMove)) {
            PieceColor winner = (sideToMove == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;
            return "Checkmate! " + winner + " wins!";
        }

        if (board.isStalemate(sideToMove)) {
            return "Stalemate! Game drawn.";
        }

        if (board.isInsufficientMaterial()) {
            return "Draw by insufficient material.";
        }

        if (game != null && game.hasThreefoldRepetition()) {
            return "Draw by threefold repetition.";
        }

        return null;
    }

    public static List<Move> legalMovesForPiece(Board board, PieceColor color, chess.model.Position from) {
        Piece p = board.getPieceAt(from);
        if (p == null || p.getColor() != color) {
            return java.util.Collections.emptyList();
        }
        return board.getPossibleMovesForPiece(from);
    }

    /**
     * Validates and applies a move to the given game, returning a compact result.
     *
     * Keeps the mutation logic inside Game (clocks/history/turn), but computes
     * consequences here.
     */
    public static MoveResult applyMove(Game game, Move move) {
        PieceColor currentTurn = game.getTurn();
        if (game.isGameOver()) {
            return MoveResult.notApplied(move, currentTurn);
        }

        boolean applied = game.applyMove(move);
        if (!applied) {
            return MoveResult.notApplied(move, currentTurn);
        }

        Piece captured = game.getLastCapturedPiece();

        PieceColor nextTurn = game.getTurn();
        Board board = game.getBoard();

        boolean check = isInCheck(board, nextTurn);
        boolean checkmate = board.isCheckmate(nextTurn);
        boolean stalemate = board.isStalemate(nextTurn);
        boolean insufficient = board.isInsufficientMaterial();
        boolean threefoldRepetition = game.hasThreefoldRepetition();
        boolean gameOver = game.isGameOver();
        String gameResult = game.getGameResult();

        return new MoveResult(
                true,
                move,
                nextTurn,
                captured,
                check,
                checkmate,
                stalemate,
                insufficient,
                threefoldRepetition,
                gameOver,
                gameResult);
    }
}
