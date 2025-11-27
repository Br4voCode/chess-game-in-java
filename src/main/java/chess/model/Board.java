package chess.model;

import chess.model.pieces.*;

import java.util.ArrayList;
import java.util.List;

public class Board {
    private Piece[][] grid = new Piece[8][8];

    public Board() { initialize(); }

    /**
     * Standard starting position.
     */
    public void initialize() {
        // clear
        for (int r = 0; r < 8; r++) for (int c = 0; c < 8; c++) grid[r][c] = null;

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
        grid[pos.getRow()][pos.getCol()] = piece;
    }

    /**
     * Apply move to this board (mutates). Handles pawn promotion to queen by default.
     */
    public void movePiece(Move move) {
        Position from = move.getFrom();
        Position to = move.getTo();
        Piece p = getPieceAt(from);
        if (p == null) return;
        // execute
        setPieceAt(to, p);
        setPieceAt(from, null);

        // promotion (if pawn reaches last rank)
        if (p.getType() == PieceType.PAWN) {
            if ((p.getColor() == PieceColor.WHITE && to.getRow() == 0) ||
                (p.getColor() == PieceColor.BLACK && to.getRow() == 7)) {
                // promote to queen by default (or Move.promotion if provided)
                Piece promotion = move.getPromotion();
                if (promotion != null) {
                    setPieceAt(to, promotion);
                } else {
                    setPieceAt(to, new Queen(p.getColor()));
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
        for (int r=0;r<8;r++) {
            for (int c=0;c<8;c++) {
                Piece p = grid[r][c];
                if (p == null || p.getColor() != color) continue;
                Position pos = new Position(r,c);
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
     * Check if given color's king is in check (i.e., any opponent pseudo-move attacks king).
     */
    public boolean isKingInCheck(PieceColor color) {
        // find king position
        Position kingPos = null;
        for (int r=0;r<8;r++) for (int c=0;c<8;c++) {
            Piece p = grid[r][c];
            if (p != null && p.getType() == PieceType.KING && p.getColor() == color) {
                kingPos = new Position(r,c);
                break;
            }
        }
        if (kingPos == null) return false; // should not happen

        // check opponent pseudo moves to see if any capture king
        PieceColor opp = color.opposite();
        for (int r=0;r<8;r++) for (int c=0;c<8;c++) {
            Piece p = grid[r][c];
            if (p == null || p.getColor() != opp) continue;
            Position pos = new Position(r,c);
            List<Move> pseudo = p.getPseudoLegalMoves(this, pos);
            for (Move m : pseudo) {
                if (m.getTo().equals(kingPos)) return true;
            }
        }
        return false;
    }

    /**
     * Produce a deep copy of this board (pieces are new instances of same type/color).
     * Important: piece objects are recreated (shallow copy would cause shared objects).
     */
    public Board copy() {
        Board b = new Board();
        // prevent initialize override
        for (int r=0;r<8;r++) for (int c=0;c<8;c++) b.grid[r][c] = null;
        for (int r=0;r<8;r++) for (int c=0;c<8;c++) {
            Piece p = this.grid[r][c];
            if (p == null) continue;
            PieceColor col = p.getColor();
            PieceType t = p.getType();
            Piece newP = null;
            switch (t) {
                case KING: newP = new King(col); break;
                case QUEEN: newP = new Queen(col); break;
                case ROOK: newP = new Rook(col); break;
                case BISHOP: newP = new Bishop(col); break;
                case KNIGHT: newP = new Knight(col); break;
                case PAWN: newP = new Pawn(col); break;
            }
            b.grid[r][c] = newP;
        }
        return b;
    }

    /**
     * Utility: find piece positions (used by GUI)
     */
    public Piece[][] getGridCopyForDisplay() {
        Piece[][] out = new Piece[8][8];
        for (int r=0;r<8;r++) for (int c=0;c<8;c++) out[r][c] = grid[r][c];
        return out;
    }
}
