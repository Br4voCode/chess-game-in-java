package chess.game;

import chess.ai.GameTree;
import chess.ai.MinimaxTreeSearch;
import chess.ai.SimpleEvaluator;
import chess.model.Board;
import chess.model.Move;
import chess.model.PieceColor;
import chess.util.GameLogger;

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

    public int getDepth() {
        return depth;
    }

    @Override
    public Move chooseMove(Board board) {
        GameLogger logger = GameLogger.getInstance();
        logger.log("🤖 [" + color + "] Iniciando búsqueda de profundidad " + depth);
        
        long startTime = System.currentTimeMillis();
        
        GameTree tree = new GameTree(board, color);
        logger.log("🌳 Construyendo árbol de búsqueda...");
        tree.buildToDepth(depth);
        long treeTime = System.currentTimeMillis() - startTime;
        logger.log("✓ Árbol construido en " + treeTime + "ms");
        
        SimpleEvaluator se = new SimpleEvaluator();
        MinimaxTreeSearch.BoardEvaluator be = (b, perspective) -> se.evaluate(b, perspective);
        MinimaxTreeSearch search = new MinimaxTreeSearch(tree, be, color);
        
        logger.log("📊 Evaluando posiciones con minimax...");
        Move bestMove = search.runAndGetBestMove();
        long totalTime = System.currentTimeMillis() - startTime;
        logger.log("✨ Movimiento seleccionado en " + totalTime + "ms");
        
        return bestMove;
    }
}

