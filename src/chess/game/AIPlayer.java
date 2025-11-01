package chess.game;

import chess.ai.*;
import chess.model.Board;
import chess.model.PieceColor;
import chess.model.Move;

/**
 * AI that uses explicit GameTree + MinimaxTreeSearch.
 */
public class AIPlayer extends Player {
    private final PieceColor color;
    private final int depth;

    public AIPlayer(PieceColor color, int depth) {
        this.color = color;
        this.depth = depth;
    }

    @Override
    public Move chooseMove(Board board) {
        GameTree tree = new GameTree(board, color);
        tree.buildToDepth(depth);
        SimpleEvaluator se = new SimpleEvaluator();
        MinimaxTreeSearch.BoardEvaluator be = (b, perspective) -> se.evaluate(b, perspective);
        MinimaxTreeSearch search = new MinimaxTreeSearch(tree, be, color);
        return search.runAndGetBestMove();
    }
}
