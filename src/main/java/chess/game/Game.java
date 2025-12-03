package chess.game;

import chess.model.Board;
import chess.model.PieceColor;
import chess.model.Move;
import chess.model.Piece;

public class Game {
    private Board board;
    private Player white;
    private Player black;
    private PieceColor turn;
    private boolean gameOver;
    private String gameResult;
    private int moveCount;

    public Game(Player white, Player black) {
        this.board = new Board();
        this.white = white;
        this.black = black;
        this.turn = PieceColor.WHITE;
        this.gameOver = false;
        this.gameResult = null;
        this.moveCount = 0;
    }

    public Board getBoard() { 
        return board; 
    }
    
    public PieceColor getTurn() { 
        return turn; 
    }
    
    public boolean isGameOver() {
        return gameOver;
    }
    
    public String getGameResult() {
        return gameResult;
    }
    
    public int getMoveCount() {
        return moveCount;
    }

    public boolean applyMove(Move m) {
        // Check if game is over
        if (gameOver) {
            return false;
        }
        
        // Check if it's the correct player's turn
        Piece movingPiece = board.getPieceAt(m.getFrom());
        if (movingPiece == null || movingPiece.getColor() != turn) {
            return false;
        }
        
        // Check if move is legal
        if (!isMoveLegal(m)) {
            return false;
        }
        
        // Apply the move
        board.movePiece(m);
        turn = turn.opposite();
        moveCount++;
        
        // Check game state after move
        checkGameState();
        
        return true;
    }

    private boolean isMoveLegal(Move m) {
        // Get all legal moves for current player
        java.util.List<Move> legalMoves = board.getAllPossibleMoves(turn);
        
        // Check if the move is in the list of legal moves
        for (Move legalMove : legalMoves) {
            if (legalMove.getFrom().equals(m.getFrom()) && 
                legalMove.getTo().equals(m.getTo())) {
                return true;
            }
        }
        return false;
    }

    private void checkGameState() {
        // Check for checkmate
        if (board.isCheckmate(turn)) {
            gameOver = true;
            PieceColor winner = (turn == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;
            gameResult = "Checkmate! " + winner + " wins!";
            return;
        }
        
        // Check for stalemate
        if (board.isStalemate(turn)) {
            gameOver = true;
            gameResult = "Stalemate! Game drawn.";
            return;
        }
        
        // Check for insufficient material
        if (board.isInsufficientMaterial()) {
            gameOver = true;
            gameResult = "Draw by insufficient material.";
            return;
        }
        
        // Optional: Check for 50-move rule
        // Optional: Check for threefold repetition
    }

    public Move getAIMoveIfAny() {
        if (gameOver) {
            return null;
        }
        
        Player currentPlayer = (turn == PieceColor.WHITE) ? white : black;
        if (currentPlayer instanceof AIPlayer) {
            return currentPlayer.chooseMove(board);
        }
        return null;
    }

    public void reset() {
        board.initialize();
        turn = PieceColor.WHITE;
        gameOver = false;
        gameResult = null;
        moveCount = 0;
    }

    public boolean undoLastMove() {
        if (moveCount == 0) {
            return false;
        }
        
        // Note: This is a simplified undo. For a proper implementation,
        // you'd need to maintain a move history in the Board class.
        boolean success = board.undoLastMove();
        if (success) {
            turn = turn.opposite();
            moveCount--;
            gameOver = false;
            gameResult = null;
        }
        return success;
    }

    public boolean isKingInCheck(PieceColor color) {
        return board.isKingInCheck(color);
    }

    public boolean isCheckmate(PieceColor color) {
        return board.isCheckmate(color);
    }

    public boolean isStalemate(PieceColor color) {
        return board.isStalemate(color);
    }

    public boolean isInsufficientMaterial() {
        return board.isInsufficientMaterial();
    }

    public String getGameStatus() {
        if (gameOver) {
            return gameResult;
        }
        
        if (board.isKingInCheck(turn)) {
            return turn + " is in check";
        }
        
        return turn + " to move";
    }

    public Player getWhitePlayer() {
        return white;
    }

    public Player getBlackPlayer() {
        return black;
    }

    public void setPlayers(Player white, Player black) {
        this.white = white;
        this.black = black;
    }

    public Game copy() {
        Game copy = new Game(white, black);
        copy.board = this.board.copy();
        copy.turn = this.turn;
        copy.gameOver = this.gameOver;
        copy.gameResult = this.gameResult;
        copy.moveCount = this.moveCount;
        return copy;
    }
}