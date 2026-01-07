package chess.ai;

import chess.model.Board;
import chess.model.PieceColor;
import chess.model.Piece;
import chess.model.PieceType;
import chess.model.Position;
import chess.rules.RulesEngine;

public class SimpleEvaluator {

    public int evaluate(Board board, PieceColor perspective) {
        PieceColor opponent = (perspective == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;

        if (RulesEngine.isCheckmate(board, opponent)) {
            return 100000;
        }

        if (RulesEngine.isCheckmate(board, perspective)) {
            return -100000;
        }

        int score = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position pos = new Position(r, c);
                Piece p = board.getPieceAt(pos);
                if (p == null)
                    continue;
                int val = pieceValue(p.getType());
                score += (p.getColor() == perspective) ? val : -val;
            }
        }
        return score;
    }

    private int pieceValue(PieceType t) {
        switch (t) {
            case PAWN:
                return 100;
            case KNIGHT:
                return 320;
            case BISHOP:
                return 330;
            case ROOK:
                return 500;
            case QUEEN:
                return 900;
            case KING:
                return 20000;
            default:
                return 0;
        }
    }

}
