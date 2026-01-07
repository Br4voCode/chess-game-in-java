package chess.controller;

import java.util.List;
import java.util.stream.Collectors;

import chess.game.Game;
import chess.model.Board;
import chess.model.Move;
import chess.model.Piece;
import chess.model.PieceColor;
import chess.model.PieceType;
import chess.model.Position;
import chess.model.pieces.Bishop;
import chess.model.pieces.Knight;
import chess.model.pieces.Queen;
import chess.model.pieces.Rook;
import chess.view.ChessBoard;
import chess.view.PromotionDialog;
import chess.view.components.StatusBar;
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
    }

    public void onSquareClicked(Position position) {
        if (isAnimating) {
            return;
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
            List<Move> allPossibleMoves = game.getBoard().getAllPossibleMoves(currentTurn);

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
                List<Move> pieceMoves = game.getBoard().getAllPossibleMoves(currentTurn).stream()
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
                    boolean moveSuccessful = game.applyMove(move);

                    if (moveSuccessful) {
                        chessBoard.updateSingleSquare(from);
                        chessBoard.updateSingleSquare(to);
                        updateBoardAfterCastling(from, to);

                        String moveDescription = "Castling (" +
                                (to.getCol() > from.getCol() ? "Kingside" : "Queenside") + ")";

                        PieceColor opponentColor = game.getTurn();
                        if (game.getBoard().isKingInCheck(opponentColor)) {
                            moveDescription += " - CHECK!";

                            if (game.getBoard().isCheckmate(opponentColor)) {
                                moveDescription += " CHECKMATE! " +
                                        (opponentColor == PieceColor.WHITE ? "Black" : "White") +
                                        " wins!";
                                statusBar.setStatus(moveDescription);
                                game.stopClock();
                                isAnimating = false;
                                return;
                            }
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
                    boolean moveSuccessful = game.applyMove(move);

                    if (moveSuccessful) {
                        // Check if a piece was captured and update UI
                        Piece capturedPiece = game.getLastCapturedPiece();
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

                        PieceColor opponentColor = game.getTurn();
                        if (game.getBoard().isKingInCheck(opponentColor)) {
                            moveDescription += " - CHECK!";

                            if (game.getBoard().isCheckmate(opponentColor)) {
                                moveDescription += " CHECKMATE! " +
                                        (opponentColor == PieceColor.WHITE ? "Black" : "White") +
                                        " wins!";
                                statusBar.setStatus(moveDescription);
                                game.stopClock();
                                isAnimating = false;
                                return;
                            }
                        }

                        statusBar.setStatus("Move: " + moveDescription + ". " +
                                game.getTurn() + " to move.");

                        // Agregar el movimiento al historial visual
                        String moveNotation = positionToChessNotation(from) + "-" + positionToChessNotation(to);
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
        // تخطي منطق الذكاء الاصطناعي في وضع لاعبين واثنين والذكاء الاصطناعي مقابل
        // الذكاء الاصطناعي
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

    private boolean isMoveLegal(Move move, PieceColor color) {
        List<Move> legalMoves = game.getBoard().getAllPossibleMoves(color);
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
        chessBoard.clearHighlights();

        // Clear captured pieces UI
        if (gameView != null) {
            gameView.clearCapturedPieces();
        }

        updateUI();
        if (gameView != null) {
            gameView.updateTimers();
        }
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