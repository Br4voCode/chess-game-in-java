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
import chess.view.ChessBoard;
import chess.view.PromotionDialog;
import chess.view.components.StatusBar;
import chess.view.endscreen.GameEndScreen;
import chess.history.Step;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

/**
 * متحكم يدير منطق التفاعل بين الواجهة والنموذج
 */
public class GameController {
    private Game game;
    private ChessBoard chessBoard;
    private StatusBar statusBar;
    private chess.view.GameView gameView;
    private Position selectedPosition;
    private boolean isAnimating = false;
    private boolean isTwoPlayerMode = false;
    private boolean isAIVsAIMode = false;
    private boolean isHistoryNavigationLocked = false; // locked when game over

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

        chessBoard.setCurrentBoard(game.getBoard());
        game.startClock();
        updateUI();
    }

    public void setGameView(chess.view.GameView gameView) {
        this.gameView = gameView;
        updateUI(); // Sincronizar UI al vincular la vista

        // Si venimos de cargar una partida, puede haber historial aplicado.
        // Aseguramos que la navegación no quede bloqueada salvo que el juego esté realmente terminado.
        if (game != null && !game.isGameOver()) {
            isHistoryNavigationLocked = false;
        }
        updateHistoryNavigationButtons();
    }

    private void updateHistoryNavigationButtons() {
        if (gameView == null || game == null) {
            return;
        }
        if (isHistoryNavigationLocked || game.isGameOver()) {
            gameView.setHistoryNavigationEnabled(false, false);
            return;
        }
        boolean canUndo = game.getStepHistory() != null && game.getStepHistory().canUndo();
        boolean canRedo = game.getStepHistory() != null && game.getStepHistory().canRedo();
        gameView.setHistoryNavigationEnabled(canUndo, canRedo);
    }

    /**
     * Undo navigation requested from UI.
     *
     * <p>
     * NOTE: full reverse-move logic is implemented in a later step; for now this is the entry point.
     */
    public void undoStepWithAnimation() {
        if (isAnimating || game == null || gameView == null) {
            return;
        }
        if (game.isGameOver()) {
            updateHistoryNavigationButtons();
            return;
        }

        Step step = game.getStepHistory() != null ? game.getStepHistory().popForUndo() : null;
        if (step == null) {
            updateHistoryNavigationButtons();
            return;
        }

        isAnimating = true;
        chessBoard.clearHighlights();
        selectedPosition = null;

        Move forward = step.getMove();
        Move reverse = new Move(forward.getTo(), forward.getFrom());

        Runnable afterAnim = () -> {
            applyUndo(step);
            // Update UI
            gameView.removeLastMoveFromHistory();
            if (step.getCapturedPiece() != null) {
                gameView.removeLastCapturedPiece(step.getCapturedPiece().getColor());
            }
            updateBoardState();
            gameView.updateUIFromController();
            gameView.updateTimers();

            // If we navigated back, we are no longer locked by a previous terminal state.
            isHistoryNavigationLocked = false;

            // Persist current applied history (past only)
            game.getStepHistoryStore().saveApplied(game.getStepHistory());
            updateHistoryNavigationButtons();

            isAnimating = false;
        };

        if (step.isCastling()) {
            Move rookReverse = new Move(step.getRookTo(), step.getRookFrom());
            chessBoard.animateMovesSimultaneously(reverse, rookReverse, afterAnim);
        } else {
            chessBoard.animateMove(reverse, afterAnim);
        }
    }

    /**
     * Redo navigation requested from UI.
     */
    public void redoStepWithAnimation() {
        if (isAnimating || game == null || gameView == null) {
            return;
        }
        if (game.isGameOver()) {
            updateHistoryNavigationButtons();
            return;
        }

        Step step = game.getStepHistory() != null ? game.getStepHistory().popForRedo() : null;
        if (step == null) {
            updateHistoryNavigationButtons();
            return;
        }

        isAnimating = true;
        chessBoard.clearHighlights();
        selectedPosition = null;

        Move forward = step.getMove();

        Runnable afterAnim = () -> {
            applyRedo(step);

            // Update move history panel exactly like current format
            gameView.addMoveToHistoryWithColor(step.getDisplayText(), step.getMoverColor());
            if (step.getCapturedPiece() != null) {
                // Re-add captured piece to UI
                gameView.addCapturedPiece(step.getCapturedPiece().toUnicode(), step.getCapturedPiece().getColor() == PieceColor.WHITE);
            }

            updateBoardState();
            gameView.updateUIFromController();
            gameView.updateTimers();

            game.getStepHistoryStore().saveApplied(game.getStepHistory());
            updateHistoryNavigationButtons();

            isAnimating = false;
        };

        if (step.isCastling()) {
            Move rookForward = new Move(step.getRookFrom(), step.getRookTo());
            chessBoard.animateMovesSimultaneously(forward, rookForward, afterAnim);
        } else {
            chessBoard.animateMove(forward, afterAnim);
        }
    }

    private void applyUndo(Step step) {
        Board board = game.getBoard();
        Move move = step.getMove();

        // Clear any terminal state since we are moving back in time
        game.clearGameOverState();

        // Restore en passant target
        board.setEnPassantTarget(step.getEnPassantTargetBefore());

        // Move the moved piece back from TO to FROM
        Piece mover = board.getPieceAt(move.getTo());

        // Handle promotion undo: replace promoted piece with original pawn
        if (step.isPromotion()) {
            mover = step.getOriginalPawn();
        }

        board.setPieceAt(move.getFrom(), mover);
        board.setPieceAt(move.getTo(), null);

        // Restore captured piece
        if (step.getCapturedPiece() != null) {
            if (step.isEnPassant() && step.getEnPassantCapturedPawnPos() != null) {
                board.setPieceAt(step.getEnPassantCapturedPawnPos(), step.getCapturedPiece());
            } else {
                board.setPieceAt(move.getTo(), step.getCapturedPiece());
            }
        }

        // Undo castling rook movement
        if (step.isCastling() && step.getRookFrom() != null && step.getRookTo() != null) {
            Piece rook = board.getPieceAt(step.getRookTo());
            board.setPieceAt(step.getRookFrom(), rook);
            board.setPieceAt(step.getRookTo(), null);

            if (rook instanceof Rook && step.getRookHadMovedBefore() != null) {
                ((Rook) rook).setHasMoved(step.getRookHadMovedBefore());
            }
        }

        // Restore mover hasMoved flag (King/Rook)
        if (mover instanceof King && step.getMoverHadMovedBefore() != null) {
            ((King) mover).setHasMoved(step.getMoverHadMovedBefore());
        } else if (mover instanceof Rook && step.getMoverHadMovedBefore() != null) {
            ((Rook) mover).setHasMoved(step.getMoverHadMovedBefore());
        }

        // Update game counters
        game.setMoveCount(game.getMoveCount() - 1);
        game.setTurn(step.getMoverColor());
    }

    private void applyRedo(Step step) {
        Board board = game.getBoard();
        Move move = step.getMove();

        // Clear terminal state; redo might re-enter terminal state but we don't compute it here.
        game.clearGameOverState();

        // Restore en passant target to the recorded after-state
        board.setEnPassantTarget(step.getEnPassantTargetAfter());

        Piece mover = board.getPieceAt(move.getFrom());
        if (mover == null) {
            // If something went wrong, we fail softly by recomputing from current board.
            mover = board.getPieceAt(move.getTo());
        }

        // En passant capture on redo
        if (step.isEnPassant() && step.getEnPassantCapturedPawnPos() != null) {
            board.setPieceAt(step.getEnPassantCapturedPawnPos(), null);
        }

        // Normal capture on redo
        if (!step.isEnPassant() && step.getCapturedPiece() != null) {
            board.setPieceAt(move.getTo(), null);
        }

        // Move piece forward
        board.setPieceAt(move.getTo(), mover);
        board.setPieceAt(move.getFrom(), null);

        // Apply promotion piece replacement
        if (step.isPromotion() && step.getPromotedTo() != null) {
            board.setPieceAt(move.getTo(), step.getPromotedTo());
        }

        // Apply castling rook move
        if (step.isCastling() && step.getRookFrom() != null && step.getRookTo() != null) {
            Piece rook = board.getPieceAt(step.getRookFrom());
            board.setPieceAt(step.getRookTo(), rook);
            board.setPieceAt(step.getRookFrom(), null);
        }

        // Update game counters
        game.setMoveCount(game.getMoveCount() + 1);
        game.setTurn(step.getMoverColor().opposite());

        // Recompute terminal state after redo (and also after undo we already cleared it)
        String result = RulesEngine.evaluateGameResult(board, game.getTurn());
        if (result != null) {
            game.setGameOver(true, result);
            isHistoryNavigationLocked = true;
        }
    }

    public void onSquareClicked(Position position) {
        if (isAnimating) {
            return;
        }

        // In AI vs AI mode, prevent all human interaction
        if (isAIVsAIMode) {
            statusBar.setStatus("AI vs AI mode - no manual moves allowed");
            return;
        }

        // In Human vs AI mode, prevent human from moving during AI turns
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
        // CASO 1: Estás haciendo clic en la misma casilla ya seleccionada
        // (deseleccionar)
        if (selectedPosition != null && selectedPosition.equals(position)) {
            // Deseleccionar la pieza actual
            chessBoard.clearHighlights();
            selectedPosition = null;
            statusBar.setStatus(currentTurn + " to move. Select a piece.");
            return;
        }

        // CASO 2: Estás haciendo clic en otra pieza del mismo color
        if (clickedPiece != null && clickedPiece.getColor() == currentTurn) {
            // Primero, deseleccionar cualquier pieza previamente seleccionada
            if (selectedPosition != null) {
                chessBoard.clearHighlights();
            }

            // Ahora seleccionar la nueva pieza
            selectedPosition = position;
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

        // CASO 3: Haciendo clic en pieza del oponente o casilla vacía sin tener nada
        // seleccionado
        if (clickedPiece == null || clickedPiece.getColor() != currentTurn) {
            statusBar.setStatus("Select a " + currentTurn + " piece.");

            // Si ya había algo seleccionado, limpiarlo
            if (selectedPosition != null) {
                chessBoard.clearHighlights();
                selectedPosition = null;
            }
        }
    }

    private void handleMoveAttempt(Position targetPosition, PieceColor currentTurn) {
        Move attemptedMove = new Move(selectedPosition, targetPosition);

        if (isMoveLegal(attemptedMove, currentTurn)) {
            // Check if this is a pawn promotion
            Piece movingPiece = game.getBoard().getPieceAt(selectedPosition);
            if (isPawnPromotion(movingPiece, targetPosition)) {
                handlePawnPromotion(attemptedMove);
            } else {
                executeMoveWithAnimation(attemptedMove);
            }
        } else {
            Piece targetPiece = game.getBoard().getPieceAt(targetPosition);
            if (targetPiece != null && targetPiece.getColor() == currentTurn) {
                // CASO ESPECIAL: Haciendo clic en otra pieza del mismo color cuando ya hay una
                // seleccionada
                // Primero deseleccionar la actual
                chessBoard.clearHighlights();

                // Ahora seleccionar la nueva pieza
                selectedPosition = targetPosition;
        List<Move> pieceMoves = RulesEngine.legalMoves(game.getBoard(), currentTurn).stream()
                        .filter(move -> move.getFrom().equals(targetPosition))
                        .collect(Collectors.toList());

                chessBoard.highlightPossibleMoves(targetPosition, pieceMoves);
                statusBar.setStatus("Selected " + targetPiece.getType() +
                        " at " + positionToChessNotation(targetPosition) +
                        ". Choose target.");
            } else {
                // Movimiento ilegal o clic en casilla vacía/pieza enemiga
                statusBar.setStatus("Illegal move. " + currentTurn + " to move.");
                chessBoard.clearHighlights();
                selectedPosition = null;
                updateBoardState();
            }
        }
    }

    public void executeMoveWithAnimation(Move move) {
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
            boolean isCastling = isCastlingMove(move, movedPiece);

            if (isCastling) {
                animateCastling(move, () -> {
                    MoveResult result = RulesEngine.applyMove(game, move);
                    boolean moveSuccessful = result.isMoveApplied();

                    if (moveSuccessful) {
                        chessBoard.updateSingleSquare(from);
                        chessBoard.updateSingleSquare(to);
                        updateBoardAfterCastling(from, to);

                        String moveDescription = "Castling (" +
                                (to.getCol() > from.getCol() ? "Kingside" : "Queenside") + ")";

                        if (result.isCheck()) {
                            moveDescription += " - CHECK!";
                        }

                        if (result.isGameOver()) {
                            statusBar.setStatus(result.getGameResult() != null ? result.getGameResult() : moveDescription);
                            game.stopClock();
                            showEndScreenIfNeeded(result);
                            isHistoryNavigationLocked = true;
                            updateHistoryNavigationButtons();
                            isAnimating = false;
                            return;
                        }

                        statusBar.setStatus("Move: " + moveDescription + ". " +
                                game.getTurn() + " to move.");

                        // Agregar el movimiento al historial visual
                        String moveNotation = "O-" + (to.getCol() > from.getCol() ? "O" : "O-O");
                        if (gameView != null) {
                            gameView.addMoveToHistoryWithColor(moveNotation, movedPiece.getColor());
                            gameView.updateUIFromController();
                            gameView.updateTimers();
                        }

                        // Check if time expired
                        if (game.getGameClock().hasTimeExpired(game.getTurn())) {
                            game.stopClock();
                            statusBar.setStatus(game.getTurn() + " ran out of time! Game Over.");
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
                    MoveResult result = RulesEngine.applyMove(game, move);
                    boolean moveSuccessful = result.isMoveApplied();

                    if (moveSuccessful) {
                        // Check if a piece was captured and update UI
                        Piece capturedPiece = result.getCapturedPiece();
                        if (capturedPiece != null && gameView != null) {
                            String pieceSymbol = capturedPiece.toUnicode();
                            boolean isWhitePiece = capturedPiece.getColor() == PieceColor.WHITE;
                            gameView.addCapturedPiece(pieceSymbol, isWhitePiece);
                            game.clearLastCapturedPiece();
                        }

                        chessBoard.updateSingleSquare(from);
                        chessBoard.updateSingleSquare(to);

                        // For en passant captures, also update the square where the captured pawn was
                        // removed
                        if (movedPiece.getType() == PieceType.PAWN && capturedPiece != null &&
                                capturedPiece.getType() == PieceType.PAWN) {
                            // Check if this was an en passant capture (captured piece is not at the
                            // destination)
                            Position enPassantCapturedPos = new Position(
                                    movedPiece.getColor() == PieceColor.WHITE ? to.getRow() + 1 : to.getRow() - 1,
                                    to.getCol());
                            if (game.getBoard().getPieceAt(enPassantCapturedPos) == null) {
                                // This was likely an en passant capture, update that square too
                                chessBoard.updateSingleSquare(enPassantCapturedPos);
                            }
                        }

                        String moveDescription = movedPiece.getType() +
                                " " + positionToChessNotation(from) +
                                " to " + positionToChessNotation(to);

                        if (result.isCheck()) {
                            moveDescription += " - CHECK!";
                        }

                        if (result.isGameOver()) {
                            statusBar.setStatus(result.getGameResult() != null ? result.getGameResult() : moveDescription);
                            game.stopClock();
                            showEndScreenIfNeeded(result);
                            isAnimating = false;
                            return;
                        }

                        statusBar.setStatus("Move: " + moveDescription + ". " +
                                game.getTurn() + " to move.");

                        // Agregar el movimiento al historial visual
                        String moveNotation = positionToChessNotation(from) + "-" + positionToChessNotation(to);
                        if (gameView != null) {
                            gameView.addMoveToHistoryWithColor(moveNotation, movedPiece.getColor());
                            gameView.updateUIFromController();
                            gameView.updateTimers();
                            updateHistoryNavigationButtons();
                        }

                        // Check if time expired
                        if (game.getGameClock().hasTimeExpired(game.getTurn())) {
                            game.stopClock();
                            statusBar.setStatus(game.getTurn() + " ran out of time! Game Over.");
                        }

                        handleAITurn();
                    } else {
                        statusBar.setStatus("Move failed. " + game.getTurn() + " to move.");
                        updateBoardState();
                    }

                    chessBoard.highlightMove(move);
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
        // Player vs Player: ignore (we don't label which human won here).
        if (isTwoPlayerMode) {
            return;
        }

        if (gameView == null || gameView.getRoot() == null || gameView.getRoot().getScene() == null) {
            return;
        }

        // The mover is the player who just made the move.
        PieceColor moverColor = result.getNextTurn().opposite();

        GameEndScreen.Result screenResult;
        if (result.isStalemate()) {
            screenResult = GameEndScreen.Result.DRAW;
        } else if (result.isCheckmate()) {
            PieceColor whiteIsAI = (game.getWhitePlayer() instanceof AIPlayer) ? PieceColor.WHITE : null;
            PieceColor blackIsAI = (game.getBlackPlayer() instanceof AIPlayer) ? PieceColor.BLACK : null;

            // Human vs AI
            if (!isAIVsAIMode) {
                PieceColor aiColor = (whiteIsAI != null) ? PieceColor.WHITE : (blackIsAI != null ? PieceColor.BLACK : null);
                if (aiColor == null) {
                    return; // can't decide
                }
                screenResult = (moverColor == aiColor) ? GameEndScreen.Result.LOSE : GameEndScreen.Result.WIN;
            } else {
                // AI vs AI: show WIN for the AI that delivered mate.
                // (We reuse WIN/LOSE screens as "winner/loser" even if both are AI.)
                if (whiteIsAI == null || blackIsAI == null) {
                    return;
                }
                screenResult = (moverColor == PieceColor.WHITE) ? GameEndScreen.Result.WIN : GameEndScreen.Result.LOSE;
            }
        } else {
            return;
        }

        javafx.stage.Stage owner = (javafx.stage.Stage) gameView.getRoot().getScene().getWindow();
        new GameEndScreen(owner, screenResult).show();
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
        // Skip auto AI turn handling in Player vs Player and AI vs AI.
        // (AI vs AI is driven by AIMatch callbacks in GameView.)
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

        // We don't have a MoveResult here, so we build the minimal one from the current game state.
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
        statusBar.setStatus(game.getTurn() + " to move. Select a piece.");
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

    public void resetGame() {
        game.resetForNewGame();
        game.startClock(); // Start the clock for the new game
        selectedPosition = null;
        isAnimating = false;
        isHistoryNavigationLocked = false;
        chessBoard.clearHighlights();

        // Clear captured pieces UI
        if (gameView != null) {
            gameView.clearCapturedPieces();
        }

        updateUI();
        if (gameView != null) {
            gameView.updateTimers();
        }
        updateHistoryNavigationButtons();
        statusBar.setStatus("New game started. " + game.getTurn() + " to move.");
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

        // White pawn reaching rank 8 (row 0) or black pawn reaching rank 1 (row 7)
        return (piece.getColor() == PieceColor.WHITE && targetPosition.getRow() == 0) ||
                (piece.getColor() == PieceColor.BLACK && targetPosition.getRow() == 7);
    }

    private void handlePawnPromotion(Move baseMove) {
        if (isAnimating) {
            return;
        }

        Piece pawn = game.getBoard().getPieceAt(baseMove.getFrom());

        statusBar.setStatus("¡Promoción del peón! Elige tu pieza...");

        // Clear highlights before showing dialog
        chessBoard.clearHighlights();
        selectedPosition = null;

        PromotionDialog dialog = new PromotionDialog();
        PieceType selectedPieceType = dialog.showDialog(pawn.getColor());

        // Check if user cancelled the promotion
        if (selectedPieceType == null || dialog.wasCancelled()) {
            statusBar.setStatus("Promoción cancelada. " + game.getTurn() + " to move.");
            updateBoardState();
            return;
        }

        // Create the promotion piece
        Piece promotionPiece = createPromotionPiece(selectedPieceType, pawn.getColor());

        // Create move with promotion
        Move promotionMove = new Move(baseMove.getFrom(), baseMove.getTo(), promotionPiece);

        // Execute the move with animation
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
                return new Queen(color); // Fallback to queen
        }
    }

    public boolean isAIVsAIMode() {
        return isAIVsAIMode;
    }

    public void hintButton() {
        if (isAnimating)
            return;

        // Verificar si el jugador actual es IA
        PieceColor currentTurn = game.getTurn();
        chess.game.Player currentPlayer = (currentTurn == PieceColor.WHITE)
                ? game.getWhitePlayer()
                : game.getBlackPlayer();

        if (currentPlayer instanceof AIPlayer) {
            statusBar.setStatus("Las pistas no están disponibles durante el turno de la IA.");
            return;
        }

        statusBar.setStatus("Calculando pista...");

        // Usar un hilo separado para no bloquear la UI
        new Thread(() -> {
            Move hint = bestMove();

            javafx.application.Platform.runLater(() -> {
                if (hint != null) {
                    chessBoard.highlightHint(hint);
                    statusBar.setStatus("Pista: " +
                            positionToChessNotation(hint.getFrom()) + " -> " +
                            positionToChessNotation(hint.getTo()));
                } else {
                    statusBar.setStatus("No se encontró ninguna pista.");
                }
            });
        }).start();
    }

    public Move bestMove() {
        // Usar un IA temporal con profundidad 3 para calcular la mejor jugada
        // independientemente de si el jugador es humano o IA
        chess.game.AIPlayer hintAI = new chess.game.AIPlayer(game.getTurn(), 3);
        return hintAI.chooseMove(game.getBoard());
    }
}