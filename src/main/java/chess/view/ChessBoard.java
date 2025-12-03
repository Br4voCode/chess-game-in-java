package chess.view;

import chess.model.Position;
import chess.model.Board;
import chess.model.Move;
import chess.model.Piece;
import javafx.scene.layout.GridPane;

import java.util.function.Consumer;
import java.util.List;

/**
 * Componente que representa el tablero de ajedrez
 */
public class ChessBoard {
    private GridPane board;
    private ChessSquare[][] squares;
    private Consumer<Position> squareClickListener;
    private Board currentBoard;

    public ChessBoard() {
        initializeBoard();
    }

    private void initializeBoard() {
        board = new GridPane();
        squares = new ChessSquare[8][8];
        
        // Aumentamos el tamaño para los nuevos indicadores
        board.setMaxSize(480, 480);  // 60px * 8 = 480px
        board.setMinSize(480, 480);

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position position = new Position(row, col);
                ChessSquare square = new ChessSquare(position);
                
                final int r = row, c = col;
                square.setOnMouseClicked(e -> {
                    if (squareClickListener != null) {
                        squareClickListener.accept(new Position(r, c));
                    }
                });
                
                squares[row][col] = square;
                board.add(square.getRoot(), col, row);
            }
        }
    }

    public void setSquareClickListener(Consumer<Position> listener) {
        this.squareClickListener = listener;
    }

    public void setCurrentBoard(Board board) {
        this.currentBoard = board;
    }

    // Método actualizado para incluir isCaptureMove
    public void updateSquare(Position pos, String pieceSymbol, boolean isSelected, 
                           boolean isPossibleMove, boolean isCaptureMove) {
        if (isValidPosition(pos)) {
            squares[pos.getRow()][pos.getCol()].updateAppearance(
                pieceSymbol, isSelected, isPossibleMove, isCaptureMove
            );
        }
    }

    // Versión antigua para compatibilidad
    public void updateSquare(Position pos, String pieceSymbol, boolean isSelected, boolean isPossibleMove) {
        updateSquare(pos, pieceSymbol, isSelected, isPossibleMove, false);
    }

    public void clearHighlights() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                squares[row][col].clearHighlight();
            }
        }
    }

    // Método actualizado para distinguir movimientos normales de capturas
    public void highlightPossibleMoves(Position from, List<Move> possibleMoves) {
        clearHighlights();
        
        if (currentBoard == null) {
            return;
        }
        
        // Resaltar la pieza seleccionada
        Piece selectedPiece = currentBoard.getPieceAt(from);
        String selectedSymbol = selectedPiece == null ? "" : selectedPiece.toUnicode();
        updateSquare(from, selectedSymbol, true, false, false);
        
        // Resaltar movimientos posibles
        for (Move move : possibleMoves) {
            if (move.getFrom().equals(from)) {
                Position to = move.getTo();
                Piece targetPiece = currentBoard.getPieceAt(to);
                String targetSymbol = targetPiece == null ? "" : targetPiece.toUnicode();
                boolean isCapture = targetPiece != null && targetPiece.getColor() != selectedPiece.getColor();
                
                updateSquare(to, targetSymbol, false, true, isCapture);
            }
        }
    }

    // Método optimizado para mejor rendimiento
    public void highlightPossibleMovesOptimized(Position from, List<Move> possibleMoves) {
        if (currentBoard == null) {
            return;
        }
        
        // Solo limpiar casillas que estaban resaltadas
        clearHighlights();
        
        // Resaltar la pieza seleccionada
        Piece selectedPiece = currentBoard.getPieceAt(from);
        if (selectedPiece != null) {
            updateSquare(from, selectedPiece.toUnicode(), true, false, false);
        }
        
        // Resaltar movimientos posibles
        for (Move move : possibleMoves) {
            if (move.getFrom().equals(from)) {
                Position to = move.getTo();
                Piece targetPiece = currentBoard.getPieceAt(to);
                String targetSymbol = targetPiece != null ? targetPiece.toUnicode() : "";
                boolean isCapture = targetPiece != null && targetPiece.getColor() != selectedPiece.getColor();
                
                updateSquare(to, targetSymbol, false, true, isCapture);
            }
        }
    }

    private boolean isPossibleMoveDestination(Position pos, List<Move> possibleMoves) {
        for (Move move : possibleMoves) {
            if (move.getTo().equals(pos)) {
                return true;
            }
        }
        return false;
    }

    public void updateBoard(Board board) {
        this.currentBoard = board;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position pos = new Position(row, col);
                Piece piece = board.getPieceAt(pos);
                String symbol = piece != null ? piece.toUnicode() : "";
                updateSquare(pos, symbol, false, false, false);
            }
        }
    }

    // Método para actualizar solo una casilla específica
    public void updateSingleSquare(Position pos) {
        if (currentBoard != null && isValidPosition(pos)) {
            Piece piece = currentBoard.getPieceAt(pos);
            String symbol = piece != null ? piece.toUnicode() : "";
            squares[pos.getRow()][pos.getCol()].updateAppearance(symbol, false, false, false);
        }
    }

    // Método para animar un movimiento
    public void animateMove(Move move) {
        if (!isValidPosition(move.getFrom()) || !isValidPosition(move.getTo())) {
            return;
        }
        
        // Limpiar la casilla de origen
        updateSquare(move.getFrom(), "", false, false, false);
        
        // Actualizar la casilla de destino después de un breve delay
        // (En una implementación real, esto sería con Timeline/AnimationTimer)
        if (currentBoard != null) {
            Piece piece = currentBoard.getPieceAt(move.getTo());
            String symbol = piece != null ? piece.toUnicode() : "";
            updateSquare(move.getTo(), symbol, false, false, false);
        }
    }

    private boolean isValidPosition(Position pos) {
        return pos != null && 
               pos.getRow() >= 0 && pos.getRow() < 8 && 
               pos.getCol() >= 0 && pos.getCol() < 8;
    }

    public GridPane getBoard() {
        return board;
    }
    
    public ChessSquare getSquare(Position pos) {
        if (isValidPosition(pos)) {
            return squares[pos.getRow()][pos.getCol()];
        }
        return null;
    }
}