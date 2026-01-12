package chess.model;

import chess.model.pieces.*;
import java.io.Serializable;
import java.util.*;

import chess.model.pieces.PieceColor;
import chess.model.pieces.PieceType;

/**
 * Representa el tablero de ajedrez y gestiona su estado.
 * 
 * RESPONSABILIDADES (Model):
 * - Almacenar posiciones de piezas
 * - Validar movimientos según reglas básicas
 * - Gestionar estado especial (enPassant)
 * - Detectar jaque, jaque mate, tablas
 * 
 * NO RESPONSABLE DE:
 * - Historial de movimientos (responsabilidad de Game)
 * - Visualización (responsabilidad de View)
 * - Flujo de juego (responsabilidad de Game/Controller)
 */
public class Board implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;
    private static final int SIZE = 8;

    // Estado del tablero
    protected Piece[][] grid;
    protected Position enPassantTarget; // Casilla objetivo para captura al paso

    // ==================== CONSTRUCTORES ====================

    public Board() {
        this.grid = new Piece[SIZE][SIZE];
        this.enPassantTarget = null;
        initialize();
    }

    // ==================== INICIALIZACIÓN ====================

    /**
     * Inicializa el tablero con las piezas en su posición inicial
     */
    public void initialize() {
        clear();

        // Peones
        for (int col = 0; col < SIZE; col++) {
            grid[1][col] = new Pawn(PieceColor.BLACK);
            grid[6][col] = new Pawn(PieceColor.WHITE);
        }

        // Piezas blancas (fila 7)
        grid[7][0] = new Rook(PieceColor.WHITE);
        grid[7][1] = new Knight(PieceColor.WHITE);
        grid[7][2] = new Bishop(PieceColor.WHITE);
        grid[7][3] = new Queen(PieceColor.WHITE);
        grid[7][4] = new King(PieceColor.WHITE);
        grid[7][5] = new Bishop(PieceColor.WHITE);
        grid[7][6] = new Knight(PieceColor.WHITE);
        grid[7][7] = new Rook(PieceColor.WHITE);

        // Piezas negras (fila 0)
        grid[0][0] = new Rook(PieceColor.BLACK);
        grid[0][1] = new Knight(PieceColor.BLACK);
        grid[0][2] = new Bishop(PieceColor.BLACK);
        grid[0][3] = new Queen(PieceColor.BLACK);
        grid[0][4] = new King(PieceColor.BLACK);
        grid[0][5] = new Bishop(PieceColor.BLACK);
        grid[0][6] = new Knight(PieceColor.BLACK);
        grid[0][7] = new Rook(PieceColor.BLACK);

        enPassantTarget = null;
    }

    /**
     * Limpia completamente el tablero (útil para testing)
     */
    public void clear() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                grid[row][col] = null;
            }
        }
        enPassantTarget = null;
    }

    // ==================== ACCESO A PIEZAS ====================

    /**
     * Obtiene la pieza en la posición especificada
     * 
     * @param position posición del tablero
     * @return la pieza o null si no hay pieza
     */
    public Piece getPieceAt(Position position) {
        if (!position.isValid()) {
            return null;
        }
        return grid[position.getRow()][position.getCol()];
    }

    /**
     * Obtiene la pieza en la posición especificada (sobrecarga)
     * 
     * @param row fila (0-7)
     * @param col columna (0-7)
     * @return la pieza o null si no hay pieza
     */
    public Piece getPieceAt(int row, int col) {
        return getPieceAt(new Position(row, col));
    }

    /**
     * Coloca una pieza en la posición especificada
     * 
     * @param position posición destino
     * @param piece    pieza a colocar (null para vaciar)
     */
    public void setPieceAt(Position position, Piece piece) {
        if (position.isValid()) {
            grid[position.getRow()][position.getCol()] = piece;
        }
    }

    /**
     * Coloca una pieza en la posición especificada (sobrecarga)
     */
    public void setPieceAt(int row, int col, Piece piece) {
        setPieceAt(new Position(row, col), piece);
    }

    /**
     * Elimina la pieza en la posición especificada
     */
    public void removePiece(Position position) {
        setPieceAt(position, null);
    }

    /**
     * Elimina la pieza en la posición especificada (sobrecarga)
     */
    public void removePiece(int row, int col) {
        removePiece(new Position(row, col));
    }

    // ==================== MOVIMIENTOS ====================

    /**
     * Ejecuta un movimiento en el tablero.
     * Maneja: movimiento básico, capturas, en passant, enroque, promoción.
     * 
     * NOTA: El responsable de validar que el movimiento es legal es quien llama
     * este método.
     * 
     * @param move movimiento a ejecutar
     * @return pieza capturada (null si no hay captura)
     */
    public Piece movePiece(Move move) {
        Position from = move.getFrom();
        Position to = move.getTo();

        // Obtener piezas
        Piece piece = getPieceAt(from);
        Piece capturedPiece = getPieceAt(to);

        if (piece == null) {
            return null; // Movimiento inválido
        }

        // ==================== CASOS ESPECIALES ====================

        // 1. ENROQUE
        if (piece.getType() == PieceType.KING && Math.abs(to.getCol() - from.getCol()) == 2) {
            handleCastling(from, to, piece.getColor());
        }

        // 2. CAPTURA AL PASO (EN PASSANT)
        else if (piece.getType() == PieceType.PAWN &&
                to.equals(enPassantTarget)) {
            int captureRow = from.getRow();
            capturedPiece = getPieceAt(captureRow, to.getCol());
            removePiece(captureRow, to.getCol());
        }

        // 3. PROMOCIÓN DE PEÓN
        if (piece.getType() == PieceType.PAWN) {
            boolean isWhite = piece.getColor() == PieceColor.WHITE;
            boolean reachedEnd = (isWhite && to.getRow() == 0) ||
                    (!isWhite && to.getRow() == 7);

            if (reachedEnd && move.getPromotion() != null) {
                setPieceAt(from, null); // Eliminar peón
                setPieceAt(to, move.getPromotion()); // Colocar pieza promovida
            } else {
                // Movimiento normal
                performBasicMove(piece, from, to, capturedPiece);
            }
        } else {
            // Movimiento normal para otras piezas
            performBasicMove(piece, from, to, capturedPiece);
        }

        // ==================== ACTUALIZAR EN PASSANT ====================
        updateEnPassantTarget(piece, from, to);

        return capturedPiece;
    }

    /**
     * Realiza un movimiento básico (sin casos especiales)
     */
    private void performBasicMove(Piece piece, Position from, Position to, Piece capturedPiece) {
        removePiece(from);
        if (capturedPiece != null) {
            removePiece(to);
        }
        setPieceAt(to, piece);
    }

    /**
     * Maneja el movimiento de enroque
     */
    private void handleCastling(Position kingFrom, Position kingTo, PieceColor color) {
        // Mover rey
        Piece king = getPieceAt(kingFrom);
        removePiece(kingFrom);
        setPieceAt(kingTo, king);

        // Mover torre
        int rookFromCol = kingTo.getCol() > kingFrom.getCol() ? 7 : 0;
        int rookToCol = kingTo.getCol() > kingFrom.getCol() ? 5 : 3;
        Position rookFrom = new Position(kingFrom.getRow(), rookFromCol);
        Position rookTo = new Position(kingFrom.getRow(), rookToCol);

        Piece rook = getPieceAt(rookFrom);
        removePiece(rookFrom);
        setPieceAt(rookTo, rook);
    }

    /**
     * Actualiza la posición de en passant después de un movimiento
     */
    private void updateEnPassantTarget(Piece piece, Position from, Position to) {
        enPassantTarget = null;

        // Solo los peones pueden crear oportunidad de en passant
        if (piece.getType() != PieceType.PAWN) {
            return;
        }

        // Verificar si el peón se movió 2 casillas
        int rowDiff = Math.abs(to.getRow() - from.getRow());
        if (rowDiff != 2) {
            return;
        }

        // Establecer la casilla de en passant (entre las dos posiciones)
        int enPassantRow = (from.getRow() + to.getRow()) / 2;
        enPassantTarget = new Position(enPassantRow, to.getCol());
    }

    // ==================== VALIDACIÓN DE MOVIMIENTOS ====================

    /**
     * Obtiene todos los movimientos legales para un color
     * 
     * @param color color de las piezas
     * @return lista de movimientos legales
     */
    public List<Move> getAllLegalMoves(PieceColor color) {
        List<Move> legalMoves = new ArrayList<>();

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                Piece piece = grid[row][col];
                if (piece != null && piece.getColor() == color) {
                    Position pos = new Position(row, col);
                    legalMoves.addAll(getLegalMovesForPiece(pos));
                }
            }
        }

        return legalMoves;
    }

    /**
     * Obtiene todos los movimientos legales para una pieza específica
     * 
     * @param position posición de la pieza
     * @return lista de movimientos legales
     */
    public List<Move> getLegalMovesForPiece(Position position) {
        Piece piece = getPieceAt(position);
        if (piece == null) {
            return new ArrayList<>();
        }

        // Obtener movimientos pseudo-legales
        List<Move> pseudoLegal = piece.getPseudoLegalMoves(this, position);
        List<Move> legalMoves = new ArrayList<>();

        // Filtrar movimientos que dejan al rey en jaque
        for (Move move : pseudoLegal) {
            if (isMoveLegal(move, piece.getColor())) {
                legalMoves.add(move);
            }
        }

        return legalMoves;
    }

    /**
     * Verifica si un movimiento es legal (no deja al rey en jaque)
     */
    private boolean isMoveLegal(Move move, PieceColor color) {
        // Simular el movimiento
        Board copy = this.copy();
        copy.movePiece(move);

        // Verificar si el rey está en jaque después del movimiento
        return !copy.isKingInCheck(color);
    }

    /**
     * Verifica si el rey de un color está en jaque
     * 
     * @param color color del rey a verificar
     * @return true si el rey está en jaque
     */
    public boolean isKingInCheck(PieceColor color) {
        Position kingPos = findKingPosition(color);
        if (kingPos == null) {
            return false; // No hay rey (error de estado)
        }

        return isSquareUnderAttack(kingPos, color);
    }

    /**
     * Verifica si una casilla está bajo ataque por el color opuesto
     * 
     * @param position casilla a verificar
     * @param color    color del defensor (se verifica ataque del color opuesto)
     * @return true si la casilla está bajo ataque
     */
    public boolean isSquareUnderAttack(Position position, PieceColor color) {
        PieceColor attacker = color == PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE;

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                Piece piece = grid[row][col];
                if (piece != null && piece.getColor() == attacker) {
                    Position piecePos = new Position(row, col);
                    List<Move> moves = piece.getPseudoLegalMoves(this, piecePos);
                    for (Move move : moves) {
                        if (move.getTo().equals(position)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Encuentra la posición del rey de un color
     */
    private Position findKingPosition(PieceColor color) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                Piece piece = grid[row][col];
                if (piece != null && piece.getColor() == color && piece.getType() == PieceType.KING) {
                    return new Position(row, col);
                }
            }
        }
        return null;
    }

    /**
     * Verifica si hay jaque mate
     * 
     * @param color color del rey a verificar
     * @return true si hay jaque mate
     */
    public boolean isCheckmate(PieceColor color) {
        return isKingInCheck(color) && getAllLegalMoves(color).isEmpty();
    }

    /**
     * Verifica si hay tablas por ahogo (stalemate)
     * 
     * @param color color del jugador a verificar
     * @return true si hay ahogo
     */
    public boolean isStalemate(PieceColor color) {
        return !isKingInCheck(color) && getAllLegalMoves(color).isEmpty();
    }

    /**
     * Verifica si hay insuficiente material para jaque mate
     * 
     * @return true si no hay suficiente material
     */
    public boolean isInsufficientMaterial() {
        int whiteCount = 0, blackCount = 0;
        int whiteBishops = 0, whiteLights = 0, whiteKnights = 0;
        int blackBishops = 0, blackLights = 0, blackKnights = 0;

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                Piece piece = grid[row][col];
                if (piece == null)
                    continue;

                if (piece.getColor() == PieceColor.WHITE) {
                    switch (piece.getType()) {
                        case KING:
                            break;
                        case PAWN:
                        case ROOK:
                        case QUEEN:
                            return false;
                        case BISHOP:
                            whiteBishops++;
                            if ((row + col) % 2 == 0)
                                whiteLights++;
                            break;
                        case KNIGHT:
                            whiteKnights++;
                            break;
                    }
                } else {
                    switch (piece.getType()) {
                        case KING:
                            break;
                        case PAWN:
                        case ROOK:
                        case QUEEN:
                            return false;
                        case BISHOP:
                            blackBishops++;
                            if ((row + col) % 2 == 0)
                                blackLights++;
                            break;
                        case KNIGHT:
                            blackKnights++;
                            break;
                    }
                }
            }
        }

        // Solo reyes
        if (whiteBishops == 0 && whiteKnights == 0 && blackBishops == 0 && blackKnights == 0) {
            return true;
        }

        // Rey + alfil vs Rey + alfil (mismo color)
        if (whiteBishops == 1 && whiteKnights == 0 && blackBishops == 1 && blackKnights == 0) {
            return whiteLights == blackLights;
        }

        // Rey + caballo vs Rey
        if (whiteBishops == 0 && whiteKnights == 1 && blackBishops == 0 && blackKnights == 0) {
            return true;
        }
        if (blackBishops == 0 && blackKnights == 1 && whiteBishops == 0 && whiteKnights == 0) {
            return true;
        }

        return false;
    }

    /**
     * Verifica si un movimiento es una captura
     * 
     * @param move movimiento a verificar
     * @return true si es una captura
     */
    public boolean isCaptureMove(Move move) {
        Piece targetPiece = getPieceAt(move.getTo());
        if (targetPiece != null) {
            return true;
        }

        // Verificar en passant
        Piece piece = getPieceAt(move.getFrom());
        if (piece != null && piece.getType() == PieceType.PAWN &&
                move.getTo().equals(enPassantTarget)) {
            return true;
        }

        return false;
    }

    // ==================== ESTADO DEL TABLERO ====================

    /**
     * Retorna una copia del grid para acceso de solo lectura
     * (El nombre fue mejorado de getGridCopyForDisplay())
     */
    public Piece[][] getGrid() {
        Piece[][] copy = new Piece[SIZE][SIZE];
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                copy[row][col] = grid[row][col];
            }
        }
        return copy;
    }

    /**
     * Obtiene las posiciones de todas las piezas de un color
     */
    public List<Position> getPiecePositions(PieceColor color) {
        List<Position> positions = new ArrayList<>();
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                Piece piece = grid[row][col];
                if (piece != null && piece.getColor() == color) {
                    positions.add(new Position(row, col));
                }
            }
        }
        return positions;
    }

    /**
     * Obtiene la casilla objetivo de en passant
     */
    public Position getEnPassantTarget() {
        return enPassantTarget;
    }

    /**
     * Establece la casilla objetivo de en passant (útil para undo/redo)
     */
    public void setEnPassantTarget(Position target) {
        this.enPassantTarget = target;
    }

    // ==================== CLONACIÓN ====================

    /**
     * Crea una copia profunda del tablero
     * (Necesaria para algoritmos como Minimax que requieren simular movimientos)
     */
    @Override
    public Board clone() {
        try {
            Board cloned = (Board) super.clone();
            cloned.grid = new Piece[SIZE][SIZE];

            // Copiar piezas
            for (int row = 0; row < SIZE; row++) {
                for (int col = 0; col < SIZE; col++) {
                    if (grid[row][col] != null) {
                        cloned.grid[row][col] = clonePiece(grid[row][col]);
                    }
                }
            }

            // Copiar estado especial
            cloned.enPassantTarget = enPassantTarget != null
                    ? new Position(enPassantTarget.getRow(), enPassantTarget.getCol())
                    : null;

            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Board clone failed", e);
        }
    }

    /**
     * Crea una copia del tablero (alias para clone)
     */
    public Board copy() {
        return this.clone();
    }

    private Piece clonePiece(Piece original) {
        if (original == null)
            return null;

        PieceColor color = original.getColor();
        Piece cloned = null;

        switch (original.getType()) {
            case KING:
                cloned = new King(color);
                if (original instanceof King) {
                    // Preservar si el rey se ha movido (para enroque)
                    // Nota: Requiere que King tenga getters para estado de movimiento
                }
                break;
            case QUEEN:
                cloned = new Queen(color);
                break;
            case ROOK:
                cloned = new Rook(color);
                if (original instanceof Rook) {
                    // Preservar si la torre se ha movido (para enroque)
                }
                break;
            case BISHOP:
                cloned = new Bishop(color);
                break;
            case KNIGHT:
                cloned = new Knight(color);
                break;
            case PAWN:
                cloned = new Pawn(color);
                break;
        }

        return cloned;
    }

    // ==================== INFORMACIÓN DEL TABLERO ====================

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                Piece piece = grid[row][col];
                if (piece != null) {
                    sb.append(piece.toUnicode()).append(" ");
                } else {
                    sb.append(". ");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
