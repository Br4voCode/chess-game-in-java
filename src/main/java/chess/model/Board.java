package chess.model;

import chess.model.pieces.*;

import java.util.ArrayList;
import java.util.List;

public class Board {
    private Piece[][] grid = new Piece[8][8];
    private Position lastMoveFrom;
    private Position lastMoveTo;
    private Move lastMove;

    public Board() { 
        initialize(); 
    }

    /**
     * Standard starting position.
     */
    public void initialize() {
        // clear
        for (int r = 0; r < 8; r++) for (int c = 0; c < 8; c++) grid[r][c] = null;
        lastMove = null;
        lastMoveFrom = null;
        lastMoveTo = null;

        // white pieces (bottom side rows 7 and 6)
        grid[7][0] = new Rook(PieceColor.WHITE);
        grid[7][1] = new Knight(PieceColor.WHITE);
        grid[7][2] = new Bishop(PieceColor.WHITE);
        grid[7][3] = new Queen(PieceColor.WHITE);
        grid[7][4] = new King(PieceColor.WHITE);
        grid[7][5] = new Bishop(PieceColor.WHITE);
        grid[7][6] = new Knight(PieceColor.WHITE);
        grid[7][7] = new Rook(PieceColor.WHITE);
        for (int c = 0; c < 8; c++) grid[6][c] = new Pawn(PieceColor.WHITE);

        // black pieces (top rows 0 and 1)
        grid[0][0] = new Rook(PieceColor.BLACK);
        grid[0][1] = new Knight(PieceColor.BLACK);
        grid[0][2] = new Bishop(PieceColor.BLACK);
        grid[0][3] = new Queen(PieceColor.BLACK);
        grid[0][4] = new King(PieceColor.BLACK);
        grid[0][5] = new Bishop(PieceColor.BLACK);
        grid[0][6] = new Knight(PieceColor.BLACK);
        grid[0][7] = new Rook(PieceColor.BLACK);
        for (int c = 0; c < 8; c++) grid[1][c] = new Pawn(PieceColor.BLACK);
    }

    public Piece getPieceAt(Position pos) {
        if (pos == null || !pos.isValid()) return null;
        return grid[pos.getRow()][pos.getCol()];
    }

    public void setPieceAt(Position pos, Piece piece) {
        if (pos.isValid()) {
            grid[pos.getRow()][pos.getCol()] = piece;
        }
    }

    /**
     * Apply move to this board (mutates). Handles pawn promotion.
     */
    public Piece movePiece(Move move) {
        Position from = move.getFrom();
        Position to = move.getTo();
        Piece p = getPieceAt(from);
        if (p == null) return null;
        
        lastMove = move;
        lastMoveFrom = from;
        lastMoveTo = to;
        
        // Capture the piece at the destination (if any)
        Piece capturedPiece = getPieceAt(to);
        
        if (p.getType() == PieceType.KING) {
            if (from.getRow() == to.getRow() && Math.abs(from.getCol() - to.getCol()) == 2) {
                handleCastling(from, to, p.getColor());
            }
        }
        
        setPieceAt(to, p);
        setPieceAt(from, null);
        
        if (p.getType() == PieceType.KING && p instanceof chess.model.pieces.King) {
            ((chess.model.pieces.King) p).setHasMoved(true);
        }
        
        if (p.getType() == PieceType.ROOK && p instanceof chess.model.pieces.Rook) {
            ((chess.model.pieces.Rook) p).setHasMoved(true);
        }

        // Handle pawn promotion
        if (p.getType() == PieceType.PAWN) {
            if ((p.getColor() == PieceColor.WHITE && to.getRow() == 0) ||
                (p.getColor() == PieceColor.BLACK && to.getRow() == 7)) {
                
                // Use specified promotion piece or default to queen
                Piece promotionPiece = move.getPromotion();
                if (promotionPiece != null) {
                    setPieceAt(to, promotionPiece);
                } else {
                    setPieceAt(to, new Queen(p.getColor()));
                }
            }
        }
        
        return capturedPiece;
    }

    private void handleCastling(Position kingFrom, Position kingTo, PieceColor color) {
        int row = kingFrom.getRow();
        int kingFromCol = kingFrom.getCol();
        int kingToCol = kingTo.getCol();
        
        if (kingToCol > kingFromCol) {
            Position rookFrom = new Position(row, 7);
            Position rookTo = new Position(row, 5);
            Piece rook = getPieceAt(rookFrom);
            if (rook != null) {
                setPieceAt(rookTo, rook);
                setPieceAt(rookFrom, null);
                if (rook instanceof chess.model.pieces.Rook) {
                    ((chess.model.pieces.Rook) rook).setHasMoved(true);
                }
            }
        } else {
            Position rookFrom = new Position(row, 0);
            Position rookTo = new Position(row, 3);
            Piece rook = getPieceAt(rookFrom);
            if (rook != null) {
                setPieceAt(rookTo, rook);
                setPieceAt(rookFrom, null);
                if (rook instanceof chess.model.pieces.Rook) {
                    ((chess.model.pieces.Rook) rook).setHasMoved(true);
                }
            }
        }
    }

    /**
     * Return all **legal** moves for the given color (filters out moves that leave king in check).
     */
    public List<Move> getAllPossibleMoves(PieceColor color) {
        List<Move> all = new ArrayList<>();
        // generate pseudo moves
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = grid[r][c];
                if (p == null || p.getColor() != color) continue;
                Position pos = new Position(r, c);
                List<Move> pseudo = p.getPseudoLegalMoves(this, pos);
                for (Move m : pseudo) {
                    // apply on a copy to test legality
                    Board copy = this.copy();
                    copy.movePiece(m);
                    if (!copy.isKingInCheck(color)) {
                        all.add(m);
                    }
                }
            }
        }
        return all;
    }

    /**
     * Return all **legal** moves for a specific piece at a position.
     */
    public List<Move> getPossibleMovesForPiece(Position position) {
        Piece piece = getPieceAt(position);
        if (piece == null) return new ArrayList<>();
        
        PieceColor color = piece.getColor();
        List<Move> allMoves = new ArrayList<>();
        List<Move> pseudo = piece.getPseudoLegalMoves(this, position);
        
        for (Move m : pseudo) {
            // Additional validation for castling moves
            if (piece.getType() == PieceType.KING && isKingCastlingMove(position, m)) {
                if (!isValidCastlingMove(position, m, color)) {
                    continue; // Skip invalid castling moves
                }
            }
            
            Board copy = this.copy();
            copy.movePiece(m);
            if (!copy.isKingInCheck(color)) {
                allMoves.add(m);
            }
        }
        return allMoves;
    }
    
    private boolean isKingCastlingMove(Position from, Move move) {
        return from.getRow() == move.getTo().getRow() && 
               Math.abs(from.getCol() - move.getTo().getCol()) == 2;
    }
    
    private boolean isValidCastlingMove(Position kingPos, Move castlingMove, PieceColor color) {
        if (isKingInCheck(color)) {
            return false;
        }
        
        int kingFromCol = kingPos.getCol();
        int kingToCol = castlingMove.getTo().getCol();
        int row = kingPos.getRow();
        PieceColor opponent = color.opposite();
        
        if (kingToCol > kingFromCol) {
            Position f = new Position(row, 5);
            Position g = new Position(row, 6);
            return !isSquareUnderAttack(f, opponent) && !isSquareUnderAttack(g, opponent);
        } else {
            Position c = new Position(row, 2);
            Position d = new Position(row, 3);
            return !isSquareUnderAttack(c, opponent) && !isSquareUnderAttack(d, opponent);
        }
    }

    /**
     * Check if given color's king is in check (i.e., any opponent pseudo-move attacks king).
     */
    public boolean isKingInCheck(PieceColor color) {
        // find king position
        Position kingPos = null;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = grid[r][c];
                if (p != null && p.getType() == PieceType.KING && p.getColor() == color) {
                    kingPos = new Position(r, c);
                    break;
                }
            }
            if (kingPos != null) break;
        }
        if (kingPos == null) return false; // should not happen

        // check opponent pseudo moves to see if any capture king
        PieceColor opp = color.opposite();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = grid[r][c];
                if (p == null || p.getColor() != opp) continue;
                Position pos = new Position(r, c);
                List<Move> pseudo = p.getPseudoLegalMoves(this, pos);
                for (Move m : pseudo) {
                    if (m.getTo().equals(kingPos)) return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if given color is in checkmate.
     */
    public boolean isCheckmate(PieceColor color) {
        return isKingInCheck(color) && getAllPossibleMoves(color).isEmpty();
    }

    /**
     * Check if given color is in stalemate.
     */
    public boolean isStalemate(PieceColor color) {
        return !isKingInCheck(color) && getAllPossibleMoves(color).isEmpty();
    }

    /**
     * Check if the game is drawn by insufficient material.
     */
    public boolean isInsufficientMaterial() {
        int whitePieceCount = 0;
        int blackPieceCount = 0;
        boolean whiteHasNonKing = false;
        boolean blackHasNonKing = false;
        boolean hasBishopOrKnight = false;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = grid[r][c];
                if (p != null) {
                    if (p.getColor() == PieceColor.WHITE) {
                        whitePieceCount++;
                        if (p.getType() != PieceType.KING) {
                            whiteHasNonKing = true;
                            if (p.getType() == PieceType.BISHOP || p.getType() == PieceType.KNIGHT) {
                                hasBishopOrKnight = true;
                            }
                        }
                    } else {
                        blackPieceCount++;
                        if (p.getType() != PieceType.KING) {
                            blackHasNonKing = true;
                            if (p.getType() == PieceType.BISHOP || p.getType() == PieceType.KNIGHT) {
                                hasBishopOrKnight = true;
                            }
                        }
                    }
                }
            }
        }

        // Rey contra Rey
        if (whitePieceCount == 1 && blackPieceCount == 1) return true;
        
        // Rey y alfil contra Rey
        // Rey y caballo contra Rey
        if ((whitePieceCount == 2 && !whiteHasNonKing && hasBishopOrKnight) ||
            (blackPieceCount == 2 && !blackHasNonKing && hasBishopOrKnight)) {
            return true;
        }

        return false;
    }

    /**
     * Undo the last move (requires tracking moves history).
     * Simplified version - you might want to implement proper move history.
     */
    public boolean undoLastMove() {
        if (lastMove == null) return false;
        
        // This is a simplified undo - for a complete implementation
        // you'd need to store the entire move history with captured pieces
        // For now, just reinitialize the board
        initialize();
        return true;
    }

    /**
     * Produce a deep copy of this board (pieces are new instances of same type/color).
     */
    public Board copy() {
        Board b = new Board();
        // prevent initialize override
        for (int r = 0; r < 8; r++) for (int c = 0; c < 8; c++) b.grid[r][c] = null;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = this.grid[r][c];
                if (p == null) continue;
                PieceColor col = p.getColor();
                PieceType t = p.getType();
                Piece newP = null;
                switch (t) {
                    case KING: 
                        newP = new King(col);
                        if (p instanceof King) {
                            ((King) newP).setHasMoved(((King) p).hasMovedFromStart());
                        }
                        break;
                    case QUEEN: newP = new Queen(col); break;
                    case ROOK: 
                        newP = new Rook(col);
                        if (p instanceof Rook) {
                            ((Rook) newP).setHasMoved(((Rook) p).hasMovedFromStart());
                        }
                        break;
                    case BISHOP: newP = new Bishop(col); break;
                    case KNIGHT: newP = new Knight(col); break;
                    case PAWN: newP = new Pawn(col); break;
                }
                b.grid[r][c] = newP;
            }
        }
        b.lastMove = this.lastMove;
        b.lastMoveFrom = this.lastMoveFrom;
        b.lastMoveTo = this.lastMoveTo;
        return b;
    }

    /**
     * Utility: find piece positions (used by GUI)
     */
    public Piece[][] getGridCopyForDisplay() {
        Piece[][] out = new Piece[8][8];
        for (int r = 0; r < 8; r++) for (int c = 0; c < 8; c++) out[r][c] = grid[r][c];
        return out;
    }

    /**
     * Get last move information (for highlighting)
     */
    public Move getLastMove() {
        return lastMove;
    }

    public Position getLastMoveFrom() {
        return lastMoveFrom;
    }

    public Position getLastMoveTo() {
        return lastMoveTo;
    }

    /**
     * Check if a position is under attack by the given color
     */
    public boolean isSquareUnderAttack(Position position, PieceColor attackerColor) {
        if (position == null || !position.isValid()) return false;
        
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = grid[r][c];
                if (p == null || p.getColor() != attackerColor) continue;
                Position pos = new Position(r, c);
                List<Move> pseudo = p.getPseudoLegalMoves(this, pos);
                for (Move m : pseudo) {
                    if (m.getTo().equals(position)) return true;
                }
            }
        }
        return false;
    }

    /**
     * Get all pieces of a specific color
     */
    public List<Position> getPiecePositions(PieceColor color) {
        List<Position> positions = new ArrayList<>();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = grid[r][c];
                if (p != null && p.getColor() == color) {
                    positions.add(new Position(r, c));
                }
            }
        }
        return positions;
    }

    /**
     * Check if the move is a capture move
     */
    public boolean isCaptureMove(Move move) {
        Piece targetPiece = getPieceAt(move.getTo());
        return targetPiece != null;
    }

    /**
     * Clear the board (for testing/reset)
     */
    public void clear() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                grid[r][c] = null;
            }
        }
        lastMove = null;
        lastMoveFrom = null;
        lastMoveTo = null;
    }
}