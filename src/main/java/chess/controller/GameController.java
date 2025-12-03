package chess.controller;

import chess.game.Game;
import chess.model.*;
import chess.view.ChessBoard;
import chess.view.components.StatusBar;

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
    private PieceColor currentPlayerColor;

    public GameController(Game game, ChessBoard chessBoard, StatusBar statusBar) {
        this.game = game;
        this.chessBoard = chessBoard;
        this.statusBar = statusBar;
        this.selectedPosition = null;
        this.currentPlayerColor = PieceColor.WHITE; // Por defecto
        
        // Pasar el tablero actual al ChessBoard
        chessBoard.setCurrentBoard(game.getBoard());
        updateUI();
    }

    public void onSquareClicked(Position position) {
        Board board = game.getBoard();
        PieceColor currentTurn = game.getTurn();
        Piece clickedPiece = board.getPieceAt(position);

        // Si no hay pieza seleccionada
        if (selectedPosition == null) {
            handlePieceSelection(position, clickedPiece, currentTurn);
            return;
        }

        // Intentar mover desde la posición seleccionada
        handleMoveAttempt(position, currentTurn);
    }

    private void handlePieceSelection(Position position, Piece clickedPiece, PieceColor currentTurn) {
        // Verificar si se puede seleccionar esta pieza
        if (clickedPiece == null || clickedPiece.getColor() != currentTurn) {
            statusBar.setStatus("Select a " + currentTurn + " piece.");
            return;
        }
        
        // Seleccionar pieza
        selectedPosition = position;
        List<Move> allPossibleMoves = game.getBoard().getAllPossibleMoves(currentTurn);
        
        // Filtrar solo los movimientos de esta pieza específica
        List<Move> pieceMoves = allPossibleMoves.stream()
            .filter(move -> move.getFrom().equals(position))
            .collect(Collectors.toList());
        
        if (pieceMoves.isEmpty()) {
            statusBar.setStatus("No moves available for this piece.");
            selectedPosition = null;
            return;
        }
        
        // Resaltar movimientos posibles usando el nuevo método
        chessBoard.highlightPossibleMoves(position, pieceMoves);
        
        // Actualizar estado
        currentPlayerColor = clickedPiece.getColor();
        statusBar.setStatus("Selected " + clickedPiece.getType() + 
                          " at " + positionToChessNotation(position) + 
                          ". Choose target square.");
    }

    private void handleMoveAttempt(Position targetPosition, PieceColor currentTurn) {
        Move attemptedMove = new Move(selectedPosition, targetPosition);
        
        // Verificar legalidad del movimiento
        if (isMoveLegal(attemptedMove, currentTurn)) {
            executeMove(attemptedMove);
        } else {
            // Movimiento ilegal - podrías seleccionar otra pieza si es del mismo color
            Piece targetPiece = game.getBoard().getPieceAt(targetPosition);
            if (targetPiece != null && targetPiece.getColor() == currentTurn) {
                // Seleccionar la nueva pieza
                selectedPosition = targetPosition;
                List<Move> pieceMoves = game.getBoard().getAllPossibleMoves(currentTurn).stream()
                    .filter(move -> move.getFrom().equals(targetPosition))
                    .collect(Collectors.toList());
                
                chessBoard.highlightPossibleMoves(targetPosition, pieceMoves);
                statusBar.setStatus("Selected " + targetPiece.getType() + 
                                  " at " + positionToChessNotation(targetPosition) + 
                                  ". Choose target.");
            } else {
                // Movimiento inválido
                statusBar.setStatus("Illegal move. " + currentTurn + " to move.");
                selectedPosition = null;
                chessBoard.clearHighlights();
                updateBoardState();
            }
        }
    }

    private void executeMove(Move move) {
        // Guardar información del movimiento para animación/feedback
        Position from = move.getFrom();
        Position to = move.getTo();
        Piece movedPiece = game.getBoard().getPieceAt(from);
        
        // Aplicar el movimiento
        boolean moveSuccessful = game.applyMove(move);
        
        if (moveSuccessful) {
            // Limpiar selección y highlights
            selectedPosition = null;
            chessBoard.clearHighlights();
            
            // Actualizar solo las casillas afectadas para mejor rendimiento
            chessBoard.updateSingleSquare(from);
            chessBoard.updateSingleSquare(to);
            
            // Verificar si hay captura
            String moveDescription = movedPiece.getType() + 
                                   " " + positionToChessNotation(from) + 
                                   " to " + positionToChessNotation(to);
            
            // Verificar jaque
            if (isKingInCheck(game.getTurn())) {
                moveDescription += " - CHECK!";
            }
            
            statusBar.setStatus("Move: " + moveDescription + ". " + 
                              game.getTurn() + " to move.");
            
            // Manejar turno de la IA si está habilitada
            handleAITurn();
            
            // Verificar fin del juego
            checkGameEnd();
        } else {
            statusBar.setStatus("Move failed. " + game.getTurn() + " to move.");
            selectedPosition = null;
            chessBoard.clearHighlights();
            updateBoardState();
        }
    }

    private boolean isMoveLegal(Move move, PieceColor color) {
        List<Move> legalMoves = game.getBoard().getAllPossibleMoves(color);
        return legalMoves.stream()
                .anyMatch(legalMove -> legalMove.getFrom().equals(move.getFrom()) && 
                                      legalMove.getTo().equals(move.getTo()));
    }

    private boolean isKingInCheck(PieceColor color) {
        // Este método depende de tu implementación del modelo
        // Podría ser: game.getBoard().isKingInCheck(color)
        return false; // Implementa según tu modelo
    }

    private void handleAITurn() {
        if (game.getAIMoveIfAny() != null) {
            // Para una mejor experiencia, podrías añadir un pequeño delay
            Move aiMove = game.getAIMoveIfAny();
            if (aiMove != null) {
                // Aplicar el movimiento de la IA
                game.applyMove(aiMove);
                
                // Actualizar la vista
                chessBoard.updateSingleSquare(aiMove.getFrom());
                chessBoard.updateSingleSquare(aiMove.getTo());
                
                // Mostrar información del movimiento
                Piece aiPiece = game.getBoard().getPieceAt(aiMove.getTo());
                String aiMoveDescription = "AI: " + aiPiece.getType() + 
                                         " " + positionToChessNotation(aiMove.getFrom()) + 
                                         " to " + positionToChessNotation(aiMove.getTo());
                
                if (isKingInCheck(game.getTurn())) {
                    aiMoveDescription += " - CHECK!";
                }
                
                statusBar.setStatus(aiMoveDescription + ". Your turn.");
                
                // Verificar fin del juego después del movimiento de la IA
                checkGameEnd();
            }
        }
    }

    private void checkGameEnd() {
        // Verificar jaque mate
        if (game.isCheckmate(game.getTurn())) {
            PieceColor winner = (game.getTurn() == PieceColor.WHITE) ? 
                               PieceColor.BLACK : PieceColor.WHITE;
            statusBar.setStatus("CHECKMATE! " + winner + " wins!");
            disableBoardInteraction();
            return;
        }
        
        // Verificar tablas (ahogado)
        if (game.isStalemate(game.getTurn())) {
            statusBar.setStatus("STALEMATE! Game drawn.");
            disableBoardInteraction();
            return;
        }
    }

    private void disableBoardInteraction() {
        // Deshabilitar clicks en el tablero
        chessBoard.setSquareClickListener(null);
    }

    private void updateBoardState() {
        // Actualizar todo el tablero
        chessBoard.updateBoard(game.getBoard());
    }

    private void updateUI() {
        updateBoardState();
        statusBar.setStatus(game.getTurn() + " to move. Select a piece.");
    }

    // Método auxiliar para convertir posición a notación de ajedrez (ej: "e4")
    private String positionToChessNotation(Position pos) {
        if (pos == null) return "";
        char file = (char) ('a' + pos.getCol());
        int rank = 8 - pos.getRow();
        return "" + file + rank;
    }
    
    // Métodos públicos para control externo
    public void resetGame() {
        game.reset();
        selectedPosition = null;
        chessBoard.clearHighlights();
        updateUI();
        statusBar.setStatus("Game reset. " + game.getTurn() + " to move.");
    }
    
    public void undoMove() {
        if (game.undoLastMove()) {
            selectedPosition = null;
            chessBoard.clearHighlights();
            updateUI();
            statusBar.setStatus("Move undone. " + game.getTurn() + " to move.");
        } else {
            statusBar.setStatus("No moves to undo.");
        }
    }
    
    public Position getSelectedPosition() {
        return selectedPosition;
    }
    
    public PieceColor getCurrentPlayerColor() {
        return currentPlayerColor;
    }
}