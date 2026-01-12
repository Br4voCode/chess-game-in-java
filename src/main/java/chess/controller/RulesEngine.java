package chess.controller;

import chess.model.Board;
import chess.model.Move;
import chess.model.Position;
import chess.model.pieces.Piece;
import chess.model.pieces.PieceColor;
import java.util.HashSet;
import java.util.Set;

public class RulesEngine {

    // Almacena último movimiento para validar "en passant"
    private Move lastMove = null;

    // Rastrear piezas que han movido (para validar enroque)
    private Set<String> movedPieces = new HashSet<>(); // Identificadores de piezas que han movido

    /**
     * Valida si un movimiento es legal según las reglas del ajedrez
     * 
     * @param fromRow fila de origen
     * @param fromCol columna de origen
     * @param toRow   fila de destino
     * @param toCol   columna de destino
     * @param board   tablero actual
     * @return true si el movimiento es válido
     */

    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Board board) {
        // Verificar límites del tablero
        if (!isWithinBounds(toRow, toCol)) {
            return false;
        }

        // Obtener la pieza en la posición de origen
        Piece piece = board.getPieceAt(fromRow, fromCol);
        if (piece == null) {
            return false;
        }

        // Verificar si el destino es la misma posición
        if (fromRow == toRow && fromCol == toCol) {
            return false;
        }

        // Verificar si hay una pieza aliada en el destino
        Piece targetPiece = board.getPieceAt(toRow, toCol);
        if (targetPiece != null && targetPiece.getColor() == piece.getColor()) {
            return false;
        }

        // Validar movimiento según el tipo de pieza
        return isValidMoveForPiece(piece, fromRow, fromCol, toRow, toCol, board);
    }

    /**
     * Valida el movimiento específico para cada tipo de pieza
     */
    private boolean isValidMoveForPiece(Piece piece, int fromRow, int fromCol,
            int toRow, int toCol, Board board) {
        switch (piece.getType()) {
            case PAWN:
                return isValidPawnMove(piece, fromRow, fromCol, toRow, toCol, board);
            case ROOK:
                return isValidRookMove(fromRow, fromCol, toRow, toCol, board);
            case KNIGHT:
                return isValidKnightMove(fromRow, fromCol, toRow, toCol);
            case BISHOP:
                return isValidBishopMove(fromRow, fromCol, toRow, toCol, board);
            case QUEEN:
                return isValidQueenMove(fromRow, fromCol, toRow, toCol, board);
            case KING:
                return isValidKingMove(fromRow, fromCol, toRow, toCol, board);
            default:
                return false;
        }
    }

    // ==================== MOVIMIENTOS DE PIEZAS ====================

    private boolean isValidPawnMove(Piece pawn, int fromRow, int fromCol,
            int toRow, int toCol, Board board) {
        int direction = pawn.getColor().toString().equals("WHITE") ? -1 : 1;
        int startRow = pawn.getColor().toString().equals("WHITE") ? 6 : 1;

        // Movimiento hacia adelante (sin captura)
        if (fromCol == toCol && board.getPieceAt(toRow, toCol) == null) {
            // Movimiento de una casilla
            if (toRow == fromRow + direction) {
                return true;
            }
            // Movimiento de dos casillas desde posición inicial
            if (fromRow == startRow && toRow == fromRow + 2 * direction) {
                return board.getPieceAt(fromRow + direction, fromCol) == null;
            }
        }

        // Captura en diagonal
        if (Math.abs(toCol - fromCol) == 1 && toRow == fromRow + direction) {
            Piece targetPiece = board.getPieceAt(toRow, toCol);

            // Captura normal
            if (targetPiece != null && targetPiece.getColor() != pawn.getColor()) {
                return true;
            }

            // Captura "en passant" - comer peón al paso
            if (targetPiece == null && isValidEnPassant(pawn, fromRow, fromCol, toRow, toCol, board)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Valida captura "en passant" (comer peón al paso)
     * El peón enemigo debe haber movido dos casillas en el turno anterior
     */
    private boolean isValidEnPassant(Piece pawn, int fromRow, int fromCol,
            int toRow, int toCol, Board board) {
        // Solo es posible si el último movimiento fue un peón moviéndose dos casillas
        if (lastMove == null) {
            return false;
        }

        // Verificar que el peón enemigo está en la casilla correcta
        int enemyPawnRow = fromRow;
        int enemyPawnCol = toCol;
        Piece enemyPawn = board.getPieceAt(enemyPawnRow, enemyPawnCol);

        if (enemyPawn == null || !enemyPawn.getType().name().equals("PAWN")) {
            return false;
        }

        // Verificar que el peón enemigo movió dos casillas
        int moveDistance = Math.abs(lastMove.getTo().getRow() - lastMove.getFrom().getRow());
        return moveDistance == 2 && lastMove.getTo().getRow() == enemyPawnRow &&
                lastMove.getTo().getCol() == enemyPawnCol;
    }

    private boolean isValidRookMove(int fromRow, int fromCol, int toRow, int toCol, Board board) {
        // La torre se mueve en línea recta (filas o columnas)
        if (fromRow != toRow && fromCol != toCol) {
            return false;
        }

        return isPathClear(fromRow, fromCol, toRow, toCol, board);
    }

    private boolean isValidKnightMove(int fromRow, int fromCol, int toRow, int toCol) {
        // El caballo se mueve en L: 2 casillas en una dirección y 1 en perpendicular
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);

        return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
    }

    private boolean isValidBishopMove(int fromRow, int fromCol, int toRow, int toCol, Board board) {
        // El alfil se mueve en diagonal
        if (Math.abs(toRow - fromRow) != Math.abs(toCol - fromCol)) {
            return false;
        }

        return isPathClear(fromRow, fromCol, toRow, toCol, board);
    }

    private boolean isValidQueenMove(int fromRow, int fromCol, int toRow, int toCol, Board board) {
        // La reina combina movimientos de torre y alfil
        if (fromRow == toRow || fromCol == toCol ||
                Math.abs(toRow - fromRow) == Math.abs(toCol - fromCol)) {
            return isPathClear(fromRow, fromCol, toRow, toCol, board);
        }

        return false;
    }

    private boolean isValidKingMove(int fromRow, int fromCol, int toRow, int toCol, Board board) {
        // Validar enroque
        if (isCastlingMove(fromRow, fromCol, toRow, toCol)) {
            return isValidCastling(fromRow, fromCol, toRow, toCol, board);
        }

        // El rey se mueve una casilla en cualquier dirección
        return Math.abs(toRow - fromRow) <= 1 && Math.abs(toCol - fromCol) <= 1;
    }

    /**
     * Detecta si el movimiento del rey es un intento de enroque
     */
    private boolean isCastlingMove(int fromRow, int fromCol, int toRow, int toCol) {
        // El rey se mueve 2 columnas (kingside o queenside)
        return fromRow == toRow && Math.abs(toCol - fromCol) == 2;
    }

    /**
     * Valida el enroque (castling)
     * Condiciones: Rey no ha movido, Torre no ha movido, camino libre, rey no en
     * jaque
     */
    private boolean isValidCastling(int fromRow, int fromCol, int toRow, int toCol, Board board) {
        Piece king = board.getPieceAt(fromRow, fromCol);
        if (king == null || !king.getType().name().equals("KING")) {
            return false;
        }

        // Verificar que el rey nunca se ha movido
        String kingId = "KING_" + king.getColor();
        if (movedPieces.contains(kingId)) {
            return false;
        }

        // Determinar la torre (queenside o kingside)
        int rookCol = toCol > fromCol ? 7 : 0; // 7 para kingside, 0 para queenside
        Piece rook = board.getPieceAt(fromRow, rookCol);

        // Verificar que hay una torre y que nunca se ha movido
        if (rook == null || !rook.getType().name().equals("ROOK")) {
            return false;
        }

        String rookId = "ROOK_" + rook.getColor() + "_" + rookCol;
        if (movedPieces.contains(rookId)) {
            return false;
        }

        // Verificar que el camino está libre
        int minCol = Math.min(fromCol, rookCol);
        int maxCol = Math.max(fromCol, rookCol);
        for (int col = minCol + 1; col < maxCol; col++) {
            if (board.getPieceAt(fromRow, col) != null) {
                return false;
            }
        }

        // El rey no debe estar en jaque
        if (isKingInCheck(king.getColor(), board)) {
            return false;
        }

        return true;
    }

    /**
     * Detecta si un peón ha llegado a la fila de promoción
     */
    public boolean isPawnPromotion(int toRow, Piece pawn) {
        if (!pawn.getType().name().equals("PAWN")) {
            return false;
        }

        // Peón blanco llega a fila 0, peón negro a fila 7
        boolean isWhite = pawn.getColor().toString().equals("WHITE");
        return (isWhite && toRow == 0) || (!isWhite && toRow == 7);
    }

    /**
     * Verifica si el rey de un color está en jaque
     */
    public boolean isKingInCheck(PieceColor color, Board board) {
        // Encontrar el rey
        Position kingPos = findKing(color, board);
        if (kingPos == null) {
            return false; // Rey no encontrado (error)
        }

        // Verificar si alguna pieza enemiga puede atacar al rey
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPieceAt(row, col);
                if (piece != null && piece.getColor() != color) {
                    // Verificar si esta pieza enemiga puede atacar al rey
                    if (isValidMove(row, col, kingPos.getRow(), kingPos.getCol(), board)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Encuentra la posición del rey de un color específico
     */
    public Position findKing(PieceColor color, Board board) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPieceAt(row, col);
                if (piece != null && piece.getType().name().equals("KING") && piece.getColor() == color) {
                    return new Position(row, col);
                }
            }
        }
        return null;
    }

    // ==================== UTILIDADES ====================

    /**
     * Verifica si el camino entre dos posiciones está libre
     */
    private boolean isPathClear(int fromRow, int fromCol, int toRow, int toCol, Board board) {
        int rowStep = Integer.compare(toRow, fromRow);
        int colStep = Integer.compare(toCol, fromCol);

        int currentRow = fromRow + rowStep;
        int currentCol = fromCol + colStep;

        while (currentRow != toRow || currentCol != toCol) {
            if (board.getPieceAt(currentRow, currentCol) != null) {
                return false;
            }
            currentRow += rowStep;
            currentCol += colStep;
        }

        return true;
    }

    /**
     * Verifica si una posición está dentro del tablero
     */
    private boolean isWithinBounds(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    /**
     * Ejecuta un movimiento en el tablero
     * 
     * @return true si el movimiento fue exitoso
     */
    public boolean executeMove(int fromRow, int fromCol, int toRow, int toCol, Board board) {
        if (!isValidMove(fromRow, fromCol, toRow, toCol, board)) {
            return false;
        }

        Piece piece = board.getPieceAt(fromRow, fromCol);
        Piece capturedPiece = board.getPieceAt(toRow, toCol);

        // Registrar que esta pieza ha movido (importante para castling)
        recordPieceMovement(piece, fromRow, fromCol);

        // Manejar enroque
        if (piece.getType().name().equals("KING") && isCastlingMove(fromRow, fromCol, toRow, toCol)) {
            int rookCol = toCol > fromCol ? 7 : 0;
            int rookNewCol = toCol > fromCol ? 5 : 3; // f-file o d-file

            // Mover el rey
            Move kingMove = new Move(new Position(fromRow, fromCol), new Position(toRow, toCol));
            board.movePiece(kingMove);

            // Mover la torre y registrar su movimiento
            Piece rook = board.getPieceAt(fromRow, rookNewCol);
            recordPieceMovement(rook, fromRow, rookCol);
            Move rookMove = new Move(new Position(fromRow, rookCol), new Position(fromRow, rookNewCol));
            board.movePiece(rookMove);

            lastMove = kingMove;
            return true;
        }

        // Manejar en passant
        if (piece.getType().name().equals("PAWN") && capturedPiece == null &&
                Math.abs(toCol - fromCol) == 1) {
            // Captura en diagonal sin pieza destino = en passant
            board.removePiece(fromRow, toCol);
        }

        // Captura normal
        if (capturedPiece != null) {
            board.removePiece(toRow, toCol);
        }

        // Mover la pieza
        Move move = new Move(new Position(fromRow, fromCol), new Position(toRow, toCol));
        board.movePiece(move);

        // Registrar movimiento para validaciones futuras (en passant, castling)
        lastMove = move;

        return true;
    }

    /**
     * Registra que una pieza ha movido
     */
    private void recordPieceMovement(Piece piece, int fromRow, int fromCol) {
        if (piece == null)
            return;

        String pieceId;
        String type = piece.getType().name();

        // Crear ID único para la pieza
        if (type.equals("KING")) {
            pieceId = "KING_" + piece.getColor();
        } else if (type.equals("ROOK")) {
            pieceId = "ROOK_" + piece.getColor() + "_" + fromCol;
        } else {
            return; // Solo rastreamos reyes y torres para castling
        }

        movedPieces.add(pieceId);
    }

    /**
     * Registra un movimiento en el historial (para en passant)
     */
    public void recordMove(Move move) {
        this.lastMove = move;
    }

    /**
     * Verifica si es jaque mate
     */
    public boolean isCheckmate(PieceColor color, Board board) {
        // El rey debe estar en jaque
        if (!isKingInCheck(color, board)) {
            return false;
        }

        // No hay ningún movimiento legal disponible
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPieceAt(row, col);
                if (piece != null && piece.getColor() == color) {
                    // Verificar si hay algún movimiento legal para esta pieza
                    for (int toRow = 0; toRow < 8; toRow++) {
                        for (int toCol = 0; toCol < 8; toCol++) {
                            if (isValidMove(row, col, toRow, toCol, board)) {
                                return false; // Se encontró un movimiento legal
                            }
                        }
                    }
                }
            }
        }

        return true; // No hay movimientos legales
    }

    /**
     * Verifica si un movimiento deja al rey en jaque
     * Simula el movimiento y verifica si el rey quedaría en jaque
     */
    public boolean leavesKingInCheck(Piece piece, int fromRow, int fromCol, int toRow, int toCol, Board board) {
        // Hacer una copia del tablero
        Board tempBoard = board.copy();

        // Simular el movimiento
        Piece tempPiece = tempBoard.getPieceAt(fromRow, fromCol);

        if (tempPiece == null) {
            return true;
        }

        // Ejecutar el movimiento en el tablero temporal
        Move tempMove = new Move(new Position(fromRow, fromCol), new Position(toRow, toCol));
        tempBoard.movePiece(tempMove);

        // Verificar si el rey está en jaque después del movimiento
        return isKingInCheck(tempPiece.getColor(), tempBoard);
    }

    /**
     * Obtiene todos los movimientos legales para una pieza
     * (que no dejan el rey en jaque)
     */
    public java.util.List<Move> getLegalMovesForPiece(Position position, Board board) {
        java.util.List<Move> legalMoves = new java.util.ArrayList<>();
        Piece piece = board.getPieceAt(position.getRow(), position.getCol());

        if (piece == null) {
            return legalMoves;
        }

        // Iterar todas las casillas del tablero
        for (int toRow = 0; toRow < 8; toRow++) {
            for (int toCol = 0; toCol < 8; toCol++) {
                if (toRow == position.getRow() && toCol == position.getCol()) {
                    continue;
                }

                // Verificar si el movimiento es válido
                if (isValidMove(position.getRow(), position.getCol(), toRow, toCol, board)) {
                    // Verificar que no deja el rey en jaque
                    if (!leavesKingInCheck(piece, position.getRow(), position.getCol(), toRow, toCol, board)) {
                        legalMoves.add(new Move(position, new Position(toRow, toCol)));
                    }
                }
            }
        }

        return legalMoves;
    }

    // ==================== VALIDACIONES DE FIN DE JUEGO ====================

    /**
     * Verifica si un color tiene movimientos legales disponibles
     */
    public boolean hasLegalMoves(PieceColor color, Board board) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPieceAt(row, col);
                if (piece != null && piece.getColor() == color) {
                    // Verificar si hay algún movimiento legal para esta pieza
                    java.util.List<Move> legalMoves = getLegalMovesForPiece(new Position(row, col), board);
                    if (!legalMoves.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Verifica si hay AHOGADO (Rey no está en jaque pero sin movimientos legales)
     * Es una condición de tablas
     */
    public boolean isStalemate(PieceColor color, Board board) {
        // El rey NO debe estar en jaque
        if (isKingInCheck(color, board)) {
            return false;
        }

        // No hay ningún movimiento legal disponible
        return !hasLegalMoves(color, board);
    }

    /**
     * Verifica si hay material insuficiente (TABLAS)
     * Casos:
     * - Rey vs Rey
     * - Rey + Caballo vs Rey
     * - Rey + Alfil vs Rey
     * - Rey + Caballo vs Rey + Caballo (mismo color de casilla)
     */
    public boolean isInsufficientMaterial(Board board) {
        int whiteKnights = 0;
        int blackKnights = 0;
        int whiteBishops = 0;
        int blackBishops = 0;
        int whiteBishopsLight = 0;
        int whiteBishopasDark = 0;
        int blackBishopsLight = 0;
        int blackBishopsDark = 0;
        int whitePawns = 0;
        int blackPawns = 0;
        int whiteRooks = 0;
        int blackRooks = 0;
        int whiteQueens = 0;
        int blackQueens = 0;

        // Contar piezas
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPieceAt(row, col);
                if (piece == null) continue;

                String type = piece.getType().name();
                PieceColor color = piece.getColor();
                boolean isLight = (row + col) % 2 == 0; // Casilla clara

                if (type.equals("KNIGHT")) {
                    if (color == PieceColor.WHITE) whiteKnights++;
                    else blackKnights++;
                } else if (type.equals("BISHOP")) {
                    if (color == PieceColor.WHITE) {
                        whiteBishops++;
                        if (isLight) whiteBishopsLight++;
                        else whiteBishopasDark++;
                    } else {
                        blackBishops++;
                        if (isLight) blackBishopsLight++;
                        else blackBishopsDark++;
                    }
                } else if (type.equals("PAWN")) {
                    if (color == PieceColor.WHITE) whitePawns++;
                    else blackPawns++;
                } else if (type.equals("ROOK")) {
                    if (color == PieceColor.WHITE) whiteRooks++;
                    else blackRooks++;
                } else if (type.equals("QUEEN")) {
                    if (color == PieceColor.WHITE) whiteQueens++;
                    else blackQueens++;
                }
            }
        }

        // Si hay peones, torres o reinas, hay material suficiente
        if (whitePawns > 0 || blackPawns > 0 || whiteRooks > 0 || blackRooks > 0 || 
            whiteQueens > 0 || blackQueens > 0) {
            return false;
        }

        // Rey vs Rey - TABLAS
        if (whiteKnights == 0 && blackKnights == 0 && whiteBishops == 0 && blackBishops == 0) {
            return true;
        }

        // Rey + Caballo vs Rey - TABLAS
        if ((whiteKnights == 1 && blackKnights == 0 && whiteBishops == 0 && blackBishops == 0) ||
            (whiteKnights == 0 && blackKnights == 1 && whiteBishops == 0 && blackBishops == 0)) {
            return true;
        }

        // Rey + Alfil vs Rey - TABLAS
        if ((whiteBishops == 1 && whiteKnights == 0 && blackBishops == 0 && blackKnights == 0) ||
            (blackBishops == 1 && blackKnights == 0 && whiteBishops == 0 && whiteKnights == 0)) {
            return true;
        }

        // Rey + Caballo vs Rey + Caballo - TABLAS
        if (whiteKnights == 1 && whiteBishops == 0 && blackKnights == 1 && blackBishops == 0) {
            return true;
        }

        // Rey + Alfil vs Rey + Alfil (mismo color de casilla) - TABLAS
        if (whiteBishops == 1 && whiteKnights == 0 && blackBishops == 1 && blackKnights == 0) {
            if ((whiteBishopsLight > 0 && blackBishopsLight > 0) ||
                (whiteBishopasDark > 0 && blackBishopsDark > 0)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Verifica si hay TRIPLE REPETICIÓN (TABLAS)
     * Nota: Requiere historial de movimientos implementado
     */
    public boolean isThreefoldRepetition() {
        // TODO: Implementar cuando se tenga acceso al historial completo de posiciones
        return false;
    }

    /**
     * Verifica si se ha alcanzado la REGLA DE LOS 50 MOVIMIENTOS (TABLAS)
     * Sin captura de piezas ni movimiento de peones en 50 movimientos
     */
    public boolean isFiftyMoveRule() {
        // TODO: Implementar cuando se tenga acceso al historial completo de movimientos
        return false;
    }

    /**
     * MÉTODO PRINCIPAL: Verifica el estado del juego
     * Retorna:
     * - "CHECKMATE" si hay jaque mate
     * - "STALEMATE" si hay ahogado
     * - "INSUFFICIENT_MATERIAL" si hay material insuficiente
     * - "CHECK" si el rey está en jaque
     * - "IN_PROGRESS" si el juego continúa
     */
    public String getGameStatus(PieceColor currentTurnColor, Board board) {
        // Primero verificar si hay material insuficiente
        if (isInsufficientMaterial(board)) {
            return "INSUFFICIENT_MATERIAL";
        }

        // Verificar jaque mate
        if (isCheckmate(currentTurnColor, board)) {
            return "CHECKMATE";
        }

        // Verificar ahogado
        if (isStalemate(currentTurnColor, board)) {
            return "STALEMATE";
        }

        // Verificar si está en jaque
        if (isKingInCheck(currentTurnColor, board)) {
            return "CHECK";
        }

        // Juego en progreso
        return "IN_PROGRESS";
    }
}