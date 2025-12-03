package chess.controller;

import chess.game.Game;
import chess.model.*;
import chess.view.ChessBoard;
import chess.view.components.StatusBar;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador que maneja la lógica de interacción entre la vista y el modelo
 */
public class GameController {
    private Game game;
    private ChessBoard chessBoard;
    private StatusBar statusBar;
    private Position selectedPosition;
    private boolean isAnimating = false;

    public GameController(Game game, ChessBoard chessBoard, StatusBar statusBar) {
        this.game = game;
        this.chessBoard = chessBoard;
        this.statusBar = statusBar;
        this.selectedPosition = null;
        
        chessBoard.setCurrentBoard(game.getBoard());
        updateUI();
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
        // CASO 1: Estás haciendo clic en la misma casilla ya seleccionada (deseleccionar)
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
        
        // CASO 3: Haciendo clic en pieza del oponente o casilla vacía sin tener nada seleccionado
        if (clickedPiece == null || clickedPiece.getColor() != currentTurn) {
            statusBar.setStatus("Select a " + currentTurn + " piece.");
            
            // Si ya había algo seleccionado, limpiarlo
            if (selectedPosition != null) {
                chessBoard.clearHighlights();
                selectedPosition = null;
            }
            return;
        }
    }

    private void handleMoveAttempt(Position targetPosition, PieceColor currentTurn) {
        Move attemptedMove = new Move(selectedPosition, targetPosition);
        
        if (isMoveLegal(attemptedMove, currentTurn)) {
            executeMoveWithAnimation(attemptedMove);
        } else {
            Piece targetPiece = game.getBoard().getPieceAt(targetPosition);
            if (targetPiece != null && targetPiece.getColor() == currentTurn) {
                // CASO ESPECIAL: Haciendo clic en otra pieza del mismo color cuando ya hay una seleccionada
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

    private void executeMoveWithAnimation(Move move) {
        isAnimating = true;
        Position from = move.getFrom();
        Position to = move.getTo();
        Piece movedPiece = game.getBoard().getPieceAt(from);
        
        if (movedPiece == null) {
            isAnimating = false;
            return;
        }
        
        // 1. Obtener símbolo de la pieza ANTES de limpiar
        String pieceSymbol = movedPiece.toUnicode();
        
        // 2. LIMPIAR COMPLETAMENTE todos los highlights ANTES de animar
        chessBoard.clearHighlights();
        selectedPosition = null;
        
        // 3. Mostrar mensaje
        statusBar.setStatus("Moving " + movedPiece.getType() + "...");
        
        // 4. Dar un pequeño delay para asegurar que se limpió todo visualmente
        PauseTransition initialPause = new PauseTransition(Duration.millis(50));
        initialPause.setOnFinished(e -> {
            // 5. Animar el movimiento secuencialmente
            chessBoard.animateMove(move, () -> {
                // 6. Aplicar movimiento en el modelo DESPUÉS de la animación
                boolean moveSuccessful = game.applyMove(move);
                
                if (moveSuccessful) {
                    // 7. Actualizar vista con nuevo estado
                    // Nota: No necesitamos actualizar las casillas porque la animación ya lo hizo visualmente
                    // Pero sí necesitamos sincronizar el estado del modelo
                    chessBoard.updateSingleSquare(from);
                    chessBoard.updateSingleSquare(to);
                    
                    String moveDescription = movedPiece.getType() + 
                                           " " + positionToChessNotation(from) + 
                                           " to " + positionToChessNotation(to);
                    
                    // 8. Verificar estado del juego
                    PieceColor opponentColor = game.getTurn();
                    if (game.getBoard().isKingInCheck(opponentColor)) {
                        moveDescription += " - CHECK!";
                        
                        if (game.getBoard().isCheckmate(opponentColor)) {
                            moveDescription += " CHECKMATE! " + 
                                             (opponentColor == PieceColor.WHITE ? "Black" : "White") + 
                                             " wins!";
                            statusBar.setStatus(moveDescription);
                            isAnimating = false;
                            return;
                        }
                    }
                    
                    statusBar.setStatus("Move: " + moveDescription + ". " + 
                                      game.getTurn() + " to move.");
                    
                    // 9. Manejar turno de IA
                    handleAITurn();
                } else {
                    // Si el movimiento falló, restaurar vista completa
                    statusBar.setStatus("Move failed. " + game.getTurn() + " to move.");
                    updateBoardState();
                }
                
                isAnimating = false;
            });
        });
        initialPause.play();
    }

    private void handleAITurn() {
        Move aiMove = game.getAIMoveIfAny();
        if (aiMove != null) {
            new Thread(() -> {
                try {
                    Thread.sleep(800); // Delay para mejor UX
                    
                    javafx.application.Platform.runLater(() -> {
                        // Limpiar antes del movimiento de la IA
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
    }

    private String positionToChessNotation(Position pos) {
        if (pos == null) return "";
        char file = (char) ('a' + pos.getCol());
        int rank = 8 - pos.getRow();
        return "" + file + rank;
    }
    
    public void resetGame() {
        game.reset();
        selectedPosition = null;
        isAnimating = false;
        chessBoard.clearHighlights();
        updateUI();
        statusBar.setStatus("Game reset. " + game.getTurn() + " to move.");
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
}