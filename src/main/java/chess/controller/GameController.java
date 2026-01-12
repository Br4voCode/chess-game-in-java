
package chess.controller;

import java.util.List;
import java.util.stream.Collectors;

import chess.game.AIPlayer;
import chess.game.Game;
import chess.model.Board;
import chess.model.Move;
import chess.model.Piece;
import chess.model.PieceColor;
import chess.model.PieceType;
import chess.model.Position;
import chess.model.pieces.Bishop;
import chess.model.pieces.King;
import chess.model.pieces.Knight;
import chess.model.pieces.Queen;
import chess.model.pieces.Rook;
import chess.rules.MoveResult;
import chess.rules.RulesEngine;
import chess.util.SoundManager;
import chess.view.ChessBoard;
import chess.view.PromotionDialog;
import chess.view.components.StatusBar;
import chess.view.endscreen.GameEndScreen;
import chess.history.Step;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

/**
 * Controla la lógica del juego y la interacción entre la interfaz y el modelo.
 */
public class GameController {
    // --- NUEVO: Verificación periódica de tiempo agotado ---
    private javafx.animation.Timeline timeoutChecker;
    // Eliminar cualquier bandera de control de EndScreen para timeout (no se
    // necesita)

    private void startTimeoutChecker() {
        if (timeoutChecker != null) {
            timeoutChecker.stop();
        }
        timeoutChecker = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(200), e -> checkTimeoutAndShowEndScreen()));
        timeoutChecker.setCycleCount(javafx.animation.Animation.INDEFINITE);
        timeoutChecker.play();
    }

    private void stopTimeoutChecker() {
        if (timeoutChecker != null) {
            timeoutChecker.stop();
        }
    }

    private void checkTimeoutAndShowEndScreen() {
        if (game != null && !game.isGameOver() && game.getGameClock().hasTimeExpired(game.getTurn())) {
            game.stopClock();
            statusBar.setStatus(game.getTurn() + " ran out of time! Game Over.");
            showEndScreenIfNeeded(new chess.rules.MoveResult(
                    false, null, game.getTurn(), null, false, false, false, false, false, true,
                    game.getTurn() + " ran out of time! Game Over."));
            isHistoryNavigationLocked = true;
            updateHistoryNavigationButtons();
        }
    }

    private Game game;
    private ChessBoard chessBoard;
    private StatusBar statusBar;
    private chess.view.GameView gameView;
    private Position selectedPosition;
    private Move hintedMove;
    private boolean isAnimating = false;
    private boolean isTwoPlayerMode = false;
    private boolean isAIVsAIMode = false;
    private boolean isHistoryNavigationLocked = false;

    public GameController(Game game, ChessBoard chessBoard, StatusBar statusBar) {
        this(game, chessBoard, statusBar, false, false);
    }

    public GameController(Game game, ChessBoard chessBoard, StatusBar statusBar, boolean isTwoPlayerMode) {
        this(game, chessBoard, statusBar, isTwoPlayerMode, false);
    }

    public GameController(Game game, ChessBoard chessBoard, StatusBar statusBar, boolean isTwoPlayerMode,
            boolean isAIVsAIMode) {
        this.game = game;
        this.chessBoard = chessBoard;
        this.statusBar = statusBar;
        this.gameView = null;
        this.selectedPosition = null;
        this.isTwoPlayerMode = isTwoPlayerMode;
        this.isAIVsAIMode = isAIVsAIMode;
        // Reiniciar el checker de timeout siempre que se inicializa el tablero
        startTimeoutChecker();

        chessBoard.setCurrentBoard(game.getBoard());
        game.startClock();
        updateUI();
    }

    public void setGameView(chess.view.GameView gameView) {
        this.gameView = gameView;
        updateUI();
        // Reiniciar verificación de timeout cada vez que se asocia la vista
        startTimeoutChecker();
        if (game != null && !game.isGameOver()) {
            isHistoryNavigationLocked = false;
        }
        updateHistoryNavigationButtons();
        updateHintButtonState();
    }

    private void updateHistoryNavigationButtons() {
        if (gameView == null || game == null) {
            return;
        }

        // Never allow history navigation during AI vs AI.
        if (isAIVsAIMode) {
            gameView.setHistoryNavigationEnabled(false, false);
            return;
        }

        // In Player vs AI: disable undo/redo while it's the AI's turn.
        if (!isTwoPlayerMode) {
            PieceColor currentTurn = game.getTurn();
            chess.game.Player currentPlayer = (currentTurn == PieceColor.WHITE)
                    ? game.getWhitePlayer()
                    : game.getBlackPlayer();
            if (currentPlayer instanceof AIPlayer) {
                gameView.setHistoryNavigationEnabled(false, false);
                return;
            }
        }

        if (isHistoryNavigationLocked || game.isGameOver()) {
            gameView.setHistoryNavigationEnabled(false, false);
            return;
        }
        boolean canUndo = game.getStepHistory() != null && game.getStepHistory().canUndo();
        boolean canRedo = game.getStepHistory() != null && game.getStepHistory().canRedo();
        gameView.setHistoryNavigationEnabled(canUndo, canRedo);
    }

    private void updateHintButtonState() {
        if (gameView == null || game == null) {
            return;
        }

        // Disable hint button in AI vs AI mode or when game is over
        if (isAIVsAIMode || game.isGameOver()) {
            gameView.setHintButtonEnabled(false);
        } else {
            gameView.setHintButtonEnabled(true);
        }
    }

    /**
     * Undo navigation requested from UI.
     *
     * <p>
     * NOTE: full reverse-move logic is implemented in a later step; for now this is
     * the entry point.
     */
    public void undoStepWithAnimation() {
        if (isAnimating || game == null || gameView == null) {
            return;
        }

        // In Player vs AI mode, undo should revert two plies (AI move + player's move)
        // so the user can replay their move.
        int gameMode = RulesEngine.getGameModeNumber(isTwoPlayerMode, isAIVsAIMode);
        int stepsToUndo = (gameMode == 2) ? 2 : 1;

        if (game.isGameOver()) {
            updateHistoryNavigationButtons();
            return;
        }

        chess.history.StepHistory history = game.getStepHistory();
        Step step1 = history != null ? history.popForUndo() : null;
        if (step1 == null) {
            updateHistoryNavigationButtons();
            return;
        }

        final Step step2 = (stepsToUndo == 2 && history != null) ? history.popForUndo() : null;

        isAnimating = true;
        chessBoard.clearHighlights();
        selectedPosition = null;

        Runnable finishUndoAll = () -> {
            updateBoardState();
            gameView.updateUIFromController();
            gameView.updateTimers();

            isHistoryNavigationLocked = false;

            game.getStepHistoryStore().saveApplied(game.getStepHistory());
            updateHistoryNavigationButtons();

            isAnimating = false;
        };

        Runnable applyUndoStep2 = () -> {
            // After animating step2, apply its logical undo and finalize.
            applyUndo(step2);
            gameView.removeLastMoveFromHistory();
            if (step2.getCapturedPiece() != null) {
                gameView.removeLastCapturedPiece(step2.getCapturedPiece().getColor());
            }
            // Refresh only affected squares to avoid flicker.
            chessBoard.updateSquaresForStep(step2);
            gameView.updateUIFromController();
            gameView.updateTimers();
            finishUndoAll.run();
        };

        Runnable afterAnimStep1 = () -> {
            // After animating step1, apply its logical undo.
            applyUndo(step1);
            gameView.removeLastMoveFromHistory();
            if (step1.getCapturedPiece() != null) {
                gameView.removeLastCapturedPiece(step1.getCapturedPiece().getColor());
            }

            // Refresh only affected squares before starting the second animation to reduce
            // jumps.
            chessBoard.updateSquaresForStep(step1);
            gameView.updateUIFromController();
            gameView.updateTimers();

            // If PVAI and there is a second step, animate it too.
            if (step2 != null) {
                Move reverse2 = new Move(step2.getMove().getTo(), step2.getMove().getFrom());
                if (step2.isCastling()) {
                    Move rookReverse2 = new Move(step2.getRookTo(), step2.getRookFrom());
                    chessBoard.animateMovesSimultaneously(reverse2, rookReverse2, applyUndoStep2);
                } else {
                    chessBoard.animateMove(reverse2, applyUndoStep2);
                }
                return;
            }

            finishUndoAll.run();
        };

        // Animate step1 (most recent). Step2 (if any) will be animated afterwards.
        Move reverse1 = new Move(step1.getMove().getTo(), step1.getMove().getFrom());
        if (step1.isCastling()) {
            Move rookReverse1 = new Move(step1.getRookTo(), step1.getRookFrom());
            chessBoard.animateMovesSimultaneously(reverse1, rookReverse1, afterAnimStep1);
        } else {
            chessBoard.animateMove(reverse1, afterAnimStep1);
        }
    }

    /**
     * Redo navigation requested from UI.
     */
    public void redoStepWithAnimation() {
        if (isAnimating || game == null || gameView == null) {
            return;
        }

        // In Player vs AI mode, redo should apply two plies (player move + AI
        // response).
        int gameMode = RulesEngine.getGameModeNumber(isTwoPlayerMode, isAIVsAIMode);
        int stepsToRedo = (gameMode == 2) ? 2 : 1;

        if (game.isGameOver()) {
            updateHistoryNavigationButtons();
            return;
        }

        chess.history.StepHistory history = game.getStepHistory();
        Step step1 = history != null ? history.popForRedo() : null;
        if (step1 == null) {
            updateHistoryNavigationButtons();
            return;
        }

        final Step step2 = (stepsToRedo == 2 && history != null) ? history.popForRedo() : null;

        isAnimating = true;
        chessBoard.clearHighlights();
        selectedPosition = null;

        Runnable finishRedoAll = () -> {
            updateBoardState();
            gameView.updateUIFromController();
            gameView.updateTimers();

            game.getStepHistoryStore().saveApplied(game.getStepHistory());
            updateHistoryNavigationButtons();

            isAnimating = false;
        };

        Runnable applyRedoStep2 = () -> {
            applyRedo(step2);
            gameView.addMoveToHistoryWithColor(step2.getDisplayText(), step2.getMoverColor());
            if (step2.getCapturedPiece() != null) {
                gameView.addCapturedPiece(step2.getCapturedPiece().toUnicode(),
                        step2.getCapturedPiece().getColor() == PieceColor.WHITE);
            }
            // Refresh only affected squares to avoid flicker.
            chessBoard.updateSquaresForStep(step2);
            gameView.updateUIFromController();
            gameView.updateTimers();
            finishRedoAll.run();
        };

        Runnable afterAnimStep1 = () -> {
            applyRedo(step1);
            gameView.addMoveToHistoryWithColor(step1.getDisplayText(), step1.getMoverColor());
            if (step1.getCapturedPiece() != null) {
                gameView.addCapturedPiece(step1.getCapturedPiece().toUnicode(),
                        step1.getCapturedPiece().getColor() == PieceColor.WHITE);
            }

            // Refresh only affected squares before starting the second animation to reduce
            // jumps.
            chessBoard.updateSquaresForStep(step1);
            gameView.updateUIFromController();
            gameView.updateTimers();

            if (step2 != null) {
                Move forward2 = step2.getMove();
                if (step2.isCastling()) {
                    Move rookForward2 = new Move(step2.getRookFrom(), step2.getRookTo());
                    chessBoard.animateMovesSimultaneously(forward2, rookForward2, applyRedoStep2);
                } else {
                    chessBoard.animateMove(forward2, applyRedoStep2);
                }
                return;
            }

            finishRedoAll.run();
        };

        // Animate step1 (first redo). Step2 (if any) will be animated afterwards.
        Move forward1 = step1.getMove();
        if (step1.isCastling()) {
            Move rookForward1 = new Move(step1.getRookFrom(), step1.getRookTo());
            chessBoard.animateMovesSimultaneously(forward1, rookForward1, afterAnimStep1);
        } else {
            chessBoard.animateMove(forward1, afterAnimStep1);
        }
    }

    private void applyUndo(Step step) {
        Board board = game.getBoard();
        Move move = step.getMove();

        game.clearGameOverState();

        board.setEnPassantTarget(step.getEnPassantTargetBefore());

        Piece mover = board.getPieceAt(move.getTo());

        if (step.isPromotion()) {
            mover = step.getOriginalPawn();
        }

        board.setPieceAt(move.getFrom(), mover);
        board.setPieceAt(move.getTo(), null);

        if (step.getCapturedPiece() != null) {
            if (step.isEnPassant() && step.getEnPassantCapturedPawnPos() != null) {
                board.setPieceAt(step.getEnPassantCapturedPawnPos(), step.getCapturedPiece());
            } else {
                board.setPieceAt(move.getTo(), step.getCapturedPiece());
            }
        }

        if (step.isCastling() && step.getRookFrom() != null && step.getRookTo() != null) {
            Piece rook = board.getPieceAt(step.getRookTo());
            board.setPieceAt(step.getRookFrom(), rook);
            board.setPieceAt(step.getRookTo(), null);

            if (rook instanceof Rook && step.getRookHadMovedBefore() != null) {
                ((Rook) rook).setHasMoved(step.getRookHadMovedBefore());
            }
        }

        if (mover instanceof King && step.getMoverHadMovedBefore() != null) {
            ((King) mover).setHasMoved(step.getMoverHadMovedBefore());
        } else if (mover instanceof Rook && step.getMoverHadMovedBefore() != null) {
            ((Rook) mover).setHasMoved(step.getMoverHadMovedBefore());
        }

        game.setMoveCount(game.getMoveCount() - 1);
        game.setTurn(step.getMoverColor());
    }

    private void applyRedo(Step step) {
        Board board = game.getBoard();
        Move move = step.getMove();

        game.clearGameOverState();

        board.setEnPassantTarget(step.getEnPassantTargetAfter());

        Piece mover = board.getPieceAt(move.getFrom());
        if (mover == null) {

            mover = board.getPieceAt(move.getTo());
        }

        if (step.isEnPassant() && step.getEnPassantCapturedPawnPos() != null) {
            board.setPieceAt(step.getEnPassantCapturedPawnPos(), null);
        }

        if (!step.isEnPassant() && step.getCapturedPiece() != null) {
            board.setPieceAt(move.getTo(), null);
        }

        board.setPieceAt(move.getTo(), mover);
        board.setPieceAt(move.getFrom(), null);

        // --- NUEVO: Actualizar hasMoved al redescubrir el paso (redo) ---
        if (mover instanceof King) {
            ((King) mover).setHasMoved(true);
        } else if (mover instanceof Rook) {
            ((Rook) mover).setHasMoved(true);
        }

        if (step.isPromotion() && step.getPromotedTo() != null) {
            board.setPieceAt(move.getTo(), step.getPromotedTo());
        }

        if (step.isCastling() && step.getRookFrom() != null && step.getRookTo() != null) {
            Piece rook = board.getPieceAt(step.getRookFrom());
            board.setPieceAt(step.getRookTo(), rook);
            board.setPieceAt(step.getRookFrom(), null);
            if (rook instanceof Rook) {
                ((Rook) rook).setHasMoved(true);
            }
        }

        game.setMoveCount(game.getMoveCount() + 1);
        game.setTurn(step.getMoverColor().opposite());

        String result = RulesEngine.evaluateGameResult(board, game.getTurn(), game);
        if (result != null) {
            game.setGameOver(true, result);
            isHistoryNavigationLocked = true;
            if (gameView != null) {
                gameView.setHintButtonEnabled(false);
            }
        }
    }

    public void onSquareClicked(Position position) {
        if (isAnimating) {
            return;
        }

        if (isAIVsAIMode) {
            statusBar.setStatus("AI vs AI mode - no manual moves allowed");
            return;
        }

        if (!isTwoPlayerMode) {
            PieceColor currentTurn = game.getTurn();
            chess.game.Player currentPlayer = (currentTurn == PieceColor.WHITE)
                    ? game.getWhitePlayer()
                    : game.getBlackPlayer();

            if (currentPlayer instanceof AIPlayer) {
                statusBar.setStatus("Please wait for AI to move...");
                return;
            }
        }

        Board board = game.getBoard();
        PieceColor currentTurn = game.getTurn();
        Piece clickedPiece = board.getPieceAt(position);

        if (selectedPosition == null) {
            handlePieceSelection(position, clickedPiece, currentTurn);
            return;
        }

        handleMoveAttempt(position, currentTurn);
    }

    private void handlePieceSelection(Position position, Piece clickedPiece, PieceColor currentTurn) {

        if (selectedPosition != null && selectedPosition.equals(position)) {

            chessBoard.clearHighlights();
            selectedPosition = null;
            hintedMove = null;
            statusBar.setStatus(currentTurn + " to move. Select a piece.");
            return;
        }

        if (clickedPiece != null && clickedPiece.getColor() == currentTurn) {

            if (selectedPosition != null) {
                chessBoard.clearHighlights();
            }

            selectedPosition = position;
            hintedMove = null;
            List<Move> allPossibleMoves = RulesEngine.legalMoves(game.getBoard(), currentTurn);

            List<Move> pieceMoves = allPossibleMoves.stream()
                    .filter(move -> move.getFrom().equals(position))
                    .collect(Collectors.toList());

            if (pieceMoves.isEmpty()) {
                statusBar.setStatus("No moves available for this " + clickedPiece.getType() + ".");
                selectedPosition = null;
                chessBoard.clearHighlights();
                return;
            }

            chessBoard.highlightPossibleMoves(position, pieceMoves);
            statusBar.setStatus("Selected " + clickedPiece.getType() +
                    " at " + positionToChessNotation(position) +
                    ". Choose target square or click again to deselect.");
            return;
        }

        if (clickedPiece == null || clickedPiece.getColor() != currentTurn) {
            statusBar.setStatus("Select a " + currentTurn + " piece.");

            if (selectedPosition != null) {
                chessBoard.clearHighlights();
                selectedPosition = null;
            }
            hintedMove = null;
        }
    }

    private void handleMoveAttempt(Position targetPosition, PieceColor currentTurn) {
        Move attemptedMove = new Move(selectedPosition, targetPosition);

        // Check if user is clicking on the hinted destination square
        if (hintedMove != null && hintedMove.getTo().equals(targetPosition)) {
            // Prioritize the hinted move
            attemptedMove = hintedMove;
            hintedMove = null;
        }

        if (isMoveLegal(attemptedMove, currentTurn)) {

            Piece movingPiece = game.getBoard().getPieceAt(attemptedMove.getFrom());
            if (isPawnPromotion(movingPiece, targetPosition)) {
                handlePawnPromotion(attemptedMove);
            } else {
                executeMoveWithAnimation(attemptedMove);
            }
        } else {
            Piece targetPiece = game.getBoard().getPieceAt(targetPosition);
            if (targetPiece != null && targetPiece.getColor() == currentTurn) {

                chessBoard.clearHighlights();

                selectedPosition = targetPosition;
                List<Move> pieceMoves = RulesEngine.legalMoves(game.getBoard(), currentTurn).stream()
                        .filter(move -> move.getFrom().equals(targetPosition))
                        .collect(Collectors.toList());

                chessBoard.highlightPossibleMoves(targetPosition, pieceMoves);
                statusBar.setStatus("Selected " + targetPiece.getType() +
                        " at " + positionToChessNotation(targetPosition) +
                        ". Choose target.");
            } else {

                statusBar.setStatus("Illegal move. " + currentTurn + " to move.");
                chessBoard.clearHighlights();
                selectedPosition = null;
                updateBoardState();
            }
            hintedMove = null;
        }
    }

    public void executeMoveWithAnimation(Move move) {
        if (game.isGameOver()) {
            // Si el juego terminó (por tiempo u otra razón), no permitir más animaciones ni
            // movimientos
            showEndScreenIfNeeded(new chess.rules.MoveResult(
                    false, move, game.getTurn(), null, false, false, false, false, false, true,
                    game.getTurn() + " ran out of time! Game Over."));
            return;
        }
        isAnimating = true;
        Position from = move.getFrom();
        Position to = move.getTo();
        Piece movedPiece = game.getBoard().getPieceAt(from);

        if (movedPiece == null) {
            isAnimating = false;
            return;
        }

        chessBoard.clearHighlights();
        selectedPosition = null;

        statusBar.setStatus("Moving " + movedPiece.getType() + "...");

        PauseTransition initialPause = new PauseTransition(Duration.millis(50));
        initialPause.setOnFinished(e -> {
            if (game.isGameOver()) {
                // Si el juego terminó durante la animación, restaurar el tablero y mostrar
                // EndScreen
                updateBoardState(); // restaura visualmente el tablero
                isAnimating = false;
                showEndScreenIfNeeded(new chess.rules.MoveResult(
                        false, move, game.getTurn(), null, false, false, false, false, false, true,
                        game.getTurn() + " ran out of time! Game Over."));
                return;
            }
            boolean isCastling = isCastlingMove(move, movedPiece);

            if (isCastling) {
                animateCastling(move, () -> {
                    if (game.isGameOver()) {
                        updateBoardState();
                        isAnimating = false;
                        showEndScreenIfNeeded(new chess.rules.MoveResult(
                                false, move, game.getTurn(), null, false, false, false, false, false, true,
                                game.getTurn() + " ran out of time! Game Over."));
                        return;
                    }
                    MoveResult result = RulesEngine.applyMove(game, move);
                    boolean moveSuccessful = result.isMoveApplied();

                    if (moveSuccessful) {
                        // Reproducir sonido de movimiento
                        SoundManager.playSound("1.mp3");
                        
                        chessBoard.updateSingleSquare(from);
                        chessBoard.updateSingleSquare(to);
                        updateBoardAfterCastling(from, to);

                        String moveDescription = "Castling (" +
                                (to.getCol() > from.getCol() ? "Kingside" : "Queenside") + ")";

                        if (result.isCheck()) {
                            moveDescription += " - CHECK!";
                            // Highlight the King in check
                            Position kingPos = findKingPosition(game.getTurn());
                            if (kingPos != null) {
                                chessBoard.highlightKingInCheck(kingPos);
                            }
                        }

                        if (result.isGameOver()) {
                            statusBar.setStatus(
                                    result.getGameResult() != null ? result.getGameResult() : moveDescription);
                            game.stopClock();
                            showEndScreenIfNeeded(result);
                            isHistoryNavigationLocked = true;
                            updateHistoryNavigationButtons();
                            if (gameView != null) {
                                gameView.setHintButtonEnabled(false);
                            }
                            isAnimating = false;
                            return;
                        }

                        statusBar.setStatus("Move: " + moveDescription + ". " +
                                game.getTurn() + " to move.");

                        String moveNotation = "O-" + (to.getCol() > from.getCol() ? "O" : "O-O");
                        if (gameView != null) {
                            gameView.addMoveToHistoryWithColor(moveNotation, movedPiece.getColor());
                            gameView.updateUIFromController();
                            gameView.updateTimers();
                        }

                        if (game.getGameClock().hasTimeExpired(game.getTurn())) {
                            // Si se acabó el tiempo después del movimiento, congelar UI y mostrar pantalla
                            // de fin
                            game.stopClock();
                            statusBar.setStatus(game.getTurn() + " ran out of time! Game Over.");
                            showEndScreenIfNeeded(new chess.rules.MoveResult(
                                    false, move, game.getTurn(), null, false, false, false, false, false, true,
                                    game.getTurn() + " ran out of time! Game Over."));
                            isHistoryNavigationLocked = true;
                            updateHistoryNavigationButtons();
                            isAnimating = false;
                            return;
                        }

                        handleAITurn();
                    } else {
                        statusBar.setStatus("Move failed. " + game.getTurn() + " to move.");
                        updateBoardState();
                    }

                    isAnimating = false;
                });
            } else {
                chessBoard.animateMove(move, () -> {
                    if (game.isGameOver()) {
                        updateBoardState();
                        isAnimating = false;
                        showEndScreenIfNeeded(new chess.rules.MoveResult(
                                false, move, game.getTurn(), null, false, false, false, false, false, true,
                                game.getTurn() + " ran out of time! Game Over."));
                        return;
                    }
                    MoveResult result = RulesEngine.applyMove(game, move);
                    boolean moveSuccessful = result.isMoveApplied();
                    boolean isCheckMove = false;

                    if (moveSuccessful) {
                        // Reproducir sonido de movimiento
                        SoundManager.playSound("1.mp3");

                        Piece capturedPiece = result.getCapturedPiece();
                        if (capturedPiece != null && gameView != null) {
                            String pieceSymbol = capturedPiece.toUnicode();
                            boolean isWhitePiece = capturedPiece.getColor() == PieceColor.WHITE;
                            gameView.addCapturedPiece(pieceSymbol, isWhitePiece);
                            game.clearLastCapturedPiece();
                        }

                        chessBoard.updateSingleSquare(from);
                        chessBoard.updateSingleSquare(to);

                        if (movedPiece.getType() == PieceType.PAWN && capturedPiece != null &&
                                capturedPiece.getType() == PieceType.PAWN) {

                            Position enPassantCapturedPos = new Position(
                                    movedPiece.getColor() == PieceColor.WHITE ? to.getRow() + 1 : to.getRow() - 1,
                                    to.getCol());
                            if (game.getBoard().getPieceAt(enPassantCapturedPos) == null) {

                                chessBoard.updateSingleSquare(enPassantCapturedPos);
                            }
                        }

                        String moveDescription = movedPiece.getType() +
                                " " + positionToChessNotation(from) +
                                " to " + positionToChessNotation(to);

                        isCheckMove = result.isCheck();
                        if (isCheckMove) {
                            moveDescription += " - CHECK!";
                        }

                        if (result.isGameOver()) {
                            statusBar.setStatus(
                                    result.getGameResult() != null ? result.getGameResult() : moveDescription);
                            game.stopClock();
                            showEndScreenIfNeeded(result);
                            if (gameView != null) {
                                gameView.setHintButtonEnabled(false);
                            }
                            isAnimating = false;
                            return;
                        }

                        statusBar.setStatus("Move: " + moveDescription + ". " +
                                game.getTurn() + " to move.");

                        String moveNotation = positionToChessNotation(from) + "-" + positionToChessNotation(to);
                        if (gameView != null) {
                            gameView.addMoveToHistoryWithColor(moveNotation, movedPiece.getColor());
                            gameView.updateUIFromController();
                            gameView.updateTimers();
                            updateHistoryNavigationButtons();
                        }

                        if (game.getGameClock().hasTimeExpired(game.getTurn())) {
                            // Si se acabó el tiempo después del movimiento, congelar UI y mostrar pantalla
                            // de fin
                            game.stopClock();
                            statusBar.setStatus(game.getTurn() + " ran out of time! Game Over.");
                            showEndScreenIfNeeded(new chess.rules.MoveResult(
                                    false, move, game.getTurn(), null, false, false, false, false, false, true,
                                    game.getTurn() + " ran out of time! Game Over."));
                            isHistoryNavigationLocked = true;
                            updateHistoryNavigationButtons();
                            isAnimating = false;
                            return;
                        }

                        handleAITurn();
                    } else {
                        statusBar.setStatus("Move failed. " + game.getTurn() + " to move.");
                        updateBoardState();
                    }

                    chessBoard.highlightMove(move);

                    // Apply check highlighting AFTER move highlight so it's not overwritten
                    if (moveSuccessful && isCheckMove) {
                        Position kingPos = findKingPosition(game.getTurn());
                        if (kingPos != null) {
                            chessBoard.highlightKingInCheck(kingPos);
                        }
                    }

                    isAnimating = false;
                });
            }
        });
        initialPause.play();
    }

    private boolean isCastlingMove(Move move, Piece movedPiece) {
        return movedPiece.getType() == PieceType.KING &&
                move.getFrom().getRow() == move.getTo().getRow() &&
                Math.abs(move.getFrom().getCol() - move.getTo().getCol()) == 2;
    }

    private void showEndScreenIfNeeded(MoveResult result) {
        if (gameView == null || gameView.getRoot() == null || gameView.getRoot().getScene() == null) {
            return;
        }
        // Siempre detener el checker al mostrar EndScreen, pero se reiniciará al
        // reiniciar el tablero
        stopTimeoutChecker(); // Detener verificación periódica al mostrar EndScreen

        PieceColor winnerColor = result.getNextTurn().opposite();

        GameEndScreen.Result screenResult;
        String gameResultMessage = result.getGameResult();
        boolean timeout = gameResultMessage != null && gameResultMessage.contains("ran out of time");

        if (timeout) {
            // Determinar modo de juego
            boolean isPvP = isTwoPlayerMode;
            boolean isAIVsAI = isAIVsAIMode;
            // boolean isPvAI = !isPvP && !isAIVsAI; // Removed unused variable

            // El jugador que NO se quedó sin tiempo es el ganador
            // result.getNextTurn() es el que perdió por tiempo
            PieceColor loser = result.getNextTurn();
            PieceColor winner = loser.opposite();

            if (isPvP || isAIVsAI) {
                // En PvP y AIvAI, mostrar WIN para el ganador, LOSE para el perdedor
                // Suponiendo que la pantalla se muestra para ambos, aquí WIN si el jugador
                // local es el ganador
                // Para simplificar, WIN si winner es WHITE, LOSE si es BLACK (ajusta si tu UI
                // es diferente)
                screenResult = (winner == PieceColor.WHITE) ? GameEndScreen.Result.WIN : GameEndScreen.Result.LOSE;
            } else { // PvAI
                // Si el humano perdió por tiempo, LOSE; si la IA perdió por tiempo, WIN
                boolean humanIsWhite = !(game.getWhitePlayer() instanceof AIPlayer);
                boolean humanLost = (loser == PieceColor.WHITE && humanIsWhite)
                        || (loser == PieceColor.BLACK && !humanIsWhite);
                screenResult = humanLost ? GameEndScreen.Result.LOSE : GameEndScreen.Result.WIN;
            }
        } else if (result.isStalemate() || result.isInsufficientMaterial() || result.isThreefoldRepetition()) {
            screenResult = GameEndScreen.Result.DRAW;
        } else if (result.isCheckmate()) {
            if (isTwoPlayerMode) {
                screenResult = (winnerColor == PieceColor.WHITE) ? GameEndScreen.Result.WIN : GameEndScreen.Result.LOSE;
            } else if (isAIVsAIMode) {
                screenResult = (winnerColor == PieceColor.WHITE) ? GameEndScreen.Result.WIN : GameEndScreen.Result.LOSE;
            } else {
                PieceColor aiColor = (game.getWhitePlayer() instanceof AIPlayer) ? PieceColor.WHITE : PieceColor.BLACK;
                screenResult = (winnerColor == aiColor) ? GameEndScreen.Result.LOSE : GameEndScreen.Result.WIN;
            }
        } else {
            return;
        }

        javafx.stage.Stage owner = (javafx.stage.Stage) gameView.getRoot().getScene().getWindow();

        // Forzar la apertura de la EndScreen en el hilo de JavaFX
        javafx.application.Platform.runLater(() -> {
            GameEndScreen endScreen = new GameEndScreen(owner, screenResult, gameResultMessage);
            endScreen.setOnBackToMenu(() -> {
                javafx.application.Platform.runLater(() -> {
                    if (gameView != null) {
                        gameView.handleBackToMenu();
                        // Inicia una nueva partida automáticamente tras volver al menú
                        // Si quieres un retardo, puedes agregar un PauseTransition aquí
                        // gameView.triggerBackToMenu(); // Solo si necesitas forzar el menú
                        // Por defecto, handleBackToMenu() ya gestiona el flujo
                    }
                });
            });
            endScreen.show();
        });
    }

    private void animateCastling(Move kingMove, Runnable onFinished) {
        Position kingFrom = kingMove.getFrom();
        Position kingTo = kingMove.getTo();
        int row = kingFrom.getRow();

        Move rookMove;
        if (kingTo.getCol() > kingFrom.getCol()) {
            rookMove = new Move(new Position(row, 7), new Position(row, 5));
        } else {
            rookMove = new Move(new Position(row, 0), new Position(row, 3));
        }

        chessBoard.animateMovesSimultaneously(kingMove, rookMove, onFinished);
    }

    private void updateBoardAfterCastling(Position kingFrom, Position kingTo) {
        int row = kingFrom.getRow();

        if (kingTo.getCol() > kingFrom.getCol()) {
            chessBoard.updateSingleSquare(new Position(row, 7));
            chessBoard.updateSingleSquare(new Position(row, 5));
        } else {
            chessBoard.updateSingleSquare(new Position(row, 0));
            chessBoard.updateSingleSquare(new Position(row, 3));
        }
    }

    private void handleAITurn() {

        if (isTwoPlayerMode || isAIVsAIMode) {
            return;
        }

        Move aiMove = game.getAIMoveIfAny();
        if (aiMove != null) {
            new Thread(() -> {
                try {
                    Thread.sleep(800);

                    javafx.application.Platform.runLater(() -> {
                        chessBoard.clearHighlights();
                        selectedPosition = null;

                        executeMoveWithAnimation(aiMove);
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    /**
     * Allows the view to request an AI move when it's the AI's turn (e.g., when the
     * human plays with black).
     */
    public void triggerAIMoveIfNeeded() {
        handleAITurn();
    }

    /**
     * AI vs AI mode uses {@link chess.game.AIMatch} which drives moves via
     * {@code executeMoveWithAnimation}. We still want to show the end screen when
     * the match ends, so we expose a small hook for GameView.
     */
    public void onAIMatchGameOver() {
        if (!isAIVsAIMode) {
            return;
        }

        if (game == null) {
            return;
        }

        MoveResult result = RulesEngine.currentGameState(game);
        showEndScreenIfNeeded(result);
    }

    private boolean isMoveLegal(Move move, PieceColor color) {
        List<Move> legalMoves = RulesEngine.legalMoves(game.getBoard(), color);
        return legalMoves.stream()
                .anyMatch(legalMove -> legalMove.getFrom().equals(move.getFrom()) &&
                        legalMove.getTo().equals(move.getTo()));
    }

    private void updateBoardState() {
        chessBoard.updateBoard(game.getBoard());
    }

    private void updateUI() {
        updateBoardState();

        // Check if current player's King is in check
        MoveResult result = RulesEngine.currentGameState(game);
        if (result.isCheck()) {
            Position kingPos = findKingPosition(game.getTurn());
            if (kingPos != null) {
                chessBoard.highlightKingInCheck(kingPos);
            }
            statusBar.setStatus(game.getTurn() + " to move. King in CHECK!");
        } else {
            statusBar.setStatus(game.getTurn() + " to move. Select a piece.");
        }

        if (gameView != null) {
            gameView.updateUIFromController();
        }
    }

    private String positionToChessNotation(Position pos) {
        if (pos == null)
            return "";
        char file = (char) ('a' + pos.getCol());
        int rank = 8 - pos.getRow();
        return "" + file + rank;
    }

    private Position findKingPosition(PieceColor color) {
        Board board = game.getBoard();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position pos = new Position(row, col);
                Piece piece = board.getPieceAt(pos);
                if (piece != null && piece.getType() == PieceType.KING && piece.getColor() == color) {
                    return pos;
                }
            }
        }
        return null;
    }

    public void resetGame() {
        game.resetForNewGame();
        game.startClock();
        selectedPosition = null;
        hintedMove = null;
        isAnimating = false;
        isHistoryNavigationLocked = false;
        chessBoard.clearHighlights();

        if (gameView != null) {
            gameView.clearCapturedPieces();
        }

        updateUI();
        if (gameView != null) {
            gameView.updateTimers();
        }
        updateHistoryNavigationButtons();
        updateHintButtonState();
        statusBar.setStatus("New game started. " + game.getTurn() + " to move.");
        triggerAIMoveIfNeeded();
    }

    public Position getSelectedPosition() {
        return selectedPosition;
    }

    public boolean isAnimating() {
        return isAnimating;
    }

    public Board getCurrentBoard() {
        return game.getBoard();
    }

    public PieceColor getCurrentTurn() {
        return game.getTurn();
    }

    private boolean isPawnPromotion(Piece piece, Position targetPosition) {
        if (piece == null || piece.getType() != PieceType.PAWN) {
            return false;
        }

        return (piece.getColor() == PieceColor.WHITE && targetPosition.getRow() == 0) ||
                (piece.getColor() == PieceColor.BLACK && targetPosition.getRow() == 7);
    }

    private void handlePawnPromotion(Move baseMove) {
        if (isAnimating) {
            return;
        }

        Piece pawn = game.getBoard().getPieceAt(baseMove.getFrom());

        statusBar.setStatus("¡Promoción del peón! Elige tu pieza...");

        chessBoard.clearHighlights();
        selectedPosition = null;

        PromotionDialog dialog = new PromotionDialog();
        PieceType selectedPieceType = dialog.showDialog(pawn.getColor());

        if (selectedPieceType == null || dialog.wasCancelled()) {
            statusBar.setStatus("Promoción cancelada. " + game.getTurn() + " to move.");
            updateBoardState();
            return;
        }

        Piece promotionPiece = createPromotionPiece(selectedPieceType, pawn.getColor());

        Move promotionMove = new Move(baseMove.getFrom(), baseMove.getTo(), promotionPiece);

        executeMoveWithAnimation(promotionMove);
    }

    private Piece createPromotionPiece(PieceType type, PieceColor color) {
        switch (type) {
            case QUEEN:
                return new Queen(color);
            case ROOK:
                return new Rook(color);
            case BISHOP:
                return new Bishop(color);
            case KNIGHT:
                return new Knight(color);
            default:
                return new Queen(color);
        }
    }

    public boolean isAIVsAIMode() {
        return isAIVsAIMode;
    }

    public void hintButton() {
        if (isAnimating)
            return;

        PieceColor currentTurn = game.getTurn();
        chess.game.Player currentPlayer = (currentTurn == PieceColor.WHITE)
                ? game.getWhitePlayer()
                : game.getBlackPlayer();

        if (currentPlayer instanceof AIPlayer) {
            statusBar.setStatus("Las pistas no están disponibles durante el turno de la IA.");
            return;
        }

        statusBar.setStatus("Calculando pista...");

        new Thread(() -> {
            Move hint = bestMove();

            javafx.application.Platform.runLater(() -> {
                if (hint != null) {
                    hintedMove = hint;
                    chessBoard.highlightHint(hint);
                    statusBar.setStatus("Pista: " +
                            positionToChessNotation(hint.getFrom()) + " -> " +
                            positionToChessNotation(hint.getTo()));
                } else {
                    hintedMove = null;
                    statusBar.setStatus("No se encontró ninguna pista.");
                }
            });
        }).start();
    }

    public Move bestMove() {
        // Get the AI depth from the current game's AI player
        int aiDepth = 3; // Default depth
        
        PieceColor currentTurn = game.getTurn();
        chess.game.Player whitePlayer = game.getWhitePlayer();
        chess.game.Player blackPlayer = game.getBlackPlayer();
        
        // Check if either player is an AI and get their depth
        if (whitePlayer instanceof AIPlayer) {
            aiDepth = ((AIPlayer) whitePlayer).getDepth();
        } else if (blackPlayer instanceof AIPlayer) {
            aiDepth = ((AIPlayer) blackPlayer).getDepth();
        }
        
        chess.game.AIPlayer hintAI = new chess.game.AIPlayer(currentTurn, aiDepth);
        return hintAI.chooseMove(game.getBoard());
    }
}