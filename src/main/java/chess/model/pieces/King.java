package chess.model.pieces;

import chess.model.*;

import java.util.ArrayList;
import java.util.List;

import chess.model.pieces.PieceColor;
import chess.model.pieces.PieceType;

public class King extends Piece {
    private boolean hasMoved = false;

    public King(PieceColor color) { super(color, PieceType.KING); }

    public boolean hasMovedFromStart() { return hasMoved; }
    public void setHasMoved(boolean moved) { this.hasMoved = moved; }

    @Override
    public List<Move> getPseudoLegalMoves(Board board, Position pos) {
        List<Move> moves = new ArrayList<>();
        
        int[][] deltas = {{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};
        for (int[] d : deltas) {
            int r = pos.getRow() + d[0];
            int c = pos.getCol() + d[1];
            Position to = new Position(r, c);
            if (!to.isValid()) continue;
            Piece target = board.getPieceAt(to);
            if (target == null || target.getColor() != color) {
                moves.add(new Move(pos, to));
            }
        }
        
        if (!hasMoved) {
            moves.addAll(getKingsideCastlingMoves(board, pos));
            moves.addAll(getQueensideCastlingMoves(board, pos));
        }
        
        return moves;
    }
    
    private List<Move> getKingsideCastlingMoves(Board board, Position pos) {
        List<Move> moves = new ArrayList<>();
        
        int expectedRow = (color == PieceColor.WHITE) ? 7 : 0;
        int kingCol = 4;
        
        if (pos.getRow() != expectedRow || pos.getCol() != kingCol) {
            return moves;
        }
        
        Position rookPos = new Position(expectedRow, 7);
        Piece rook = board.getPieceAt(rookPos);
        if (rook == null || rook.getType() != PieceType.ROOK || rook.getColor() != color) {
            return moves;
        }
        
        if (rook instanceof Rook && ((Rook) rook).hasMovedFromStart()) {
            return moves;
        }
        
        if (board.getPieceAt(new Position(expectedRow, 5)) != null ||
            board.getPieceAt(new Position(expectedRow, 6)) != null) {
            return moves;
        }
        
        Position castlePos = new Position(expectedRow, 6);
        moves.add(new Move(pos, castlePos));
        
        return moves;
    }
    
    private List<Move> getQueensideCastlingMoves(Board board, Position pos) {
        List<Move> moves = new ArrayList<>();
        
        int expectedRow = (color == PieceColor.WHITE) ? 7 : 0;
        int kingCol = 4;
        
        if (pos.getRow() != expectedRow || pos.getCol() != kingCol) {
            return moves;
        }
        
        Position rookPos = new Position(expectedRow, 0);
        Piece rook = board.getPieceAt(rookPos);
        if (rook == null || rook.getType() != PieceType.ROOK || rook.getColor() != color) {
            return moves;
        }
        
        if (rook instanceof Rook && ((Rook) rook).hasMovedFromStart()) {
            return moves;
        }
        
        if (board.getPieceAt(new Position(expectedRow, 1)) != null ||
            board.getPieceAt(new Position(expectedRow, 2)) != null ||
            board.getPieceAt(new Position(expectedRow, 3)) != null) {
            return moves;
        }
        
        Position castlePos = new Position(expectedRow, 2);
        moves.add(new Move(pos, castlePos));
        
        return moves;
    }
}
