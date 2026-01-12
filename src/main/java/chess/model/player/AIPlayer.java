package chess.model.player;

import chess.model.ai.GameTree;
import chess.model.ai.MinimaxTreeSearch;
import chess.model.ai.SimpleEvaluator;
import chess.model.pieces.PieceColor;
import chess.model.Board;
import chess.model.Move;

/**
 * AI that uses explicit GameTree + MinimaxTreeSearch.
 */
public class AIPlayer extends Player {
    private final int difficulty;

    public AIPlayer(PieceColor color, int difficulty) {
        super(color);
        this.difficulty = difficulty;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public int getDepth() {
        return difficulty;
    }

    @Override
    public Move chooseMove(Board board) {
        System.out.println("🤖 [" + color + "] Iniciando búsqueda Minimax profundidad " + difficulty);

        long startTime = System.currentTimeMillis();

        // Crear árbol de búsqueda
        GameTree tree = new GameTree(board, color);
        System.out.println("🌳 Construyendo árbol de búsqueda...");
        tree.buildToDepth(difficulty);
        long treeTime = System.currentTimeMillis() - startTime;
        System.out.println("✓ Árbol construido en " + treeTime + "ms");

        // Crear evaluador
        SimpleEvaluator evaluator = new SimpleEvaluator();
        MinimaxTreeSearch.BoardEvaluator boardEvaluator = (b, perspective) -> evaluator.evaluate(b, perspective);

        // Ejecutar minimax
        MinimaxTreeSearch search = new MinimaxTreeSearch(tree, boardEvaluator, color);

        System.out.println("📊 Evaluando posiciones con Minimax...");
        Move bestMove = search.runAndGetBestMove();
        long totalTime = System.currentTimeMillis() - startTime;

        if (bestMove != null) {
            System.out.println("✨ Movimiento seleccionado en " + totalTime + "ms: " + bestMove);
        } else {
            System.out.println("⚠️ No hay movimiento disponible (Jaque mate o ahogado)");
        }

        return bestMove;
    }

    /*
     * @Override
     * public Move chooseMove(Board board) {
     * GameLogger logger = GameLogger.getInstance();
     * logger.log("🤖 [" + color + "] Iniciando búsqueda de profundidad " + depth);
     * 
     * long startTime = System.currentTimeMillis();
     * 
     * GameTree tree = new GameTree(board, color);
     * logger.log("🌳 Construyendo árbol de búsqueda...");
     * tree.buildToDepth(depth);
     * long treeTime = System.currentTimeMillis() - startTime;
     * logger.log("✓ Árbol construido en " + treeTime + "ms");
     * 
     * SimpleEvaluator se = new SimpleEvaluator();
     * MinimaxTreeSearch.BoardEvaluator be = (b, perspective) -> se.evaluate(b,
     * perspective);
     * MinimaxTreeSearch search = new MinimaxTreeSearch(tree, be, color);
     * 
     * logger.log("📊 Evaluando posiciones con minimax...");
     * Move bestMove = search.runAndGetBestMove();
     * long totalTime = System.currentTimeMillis() - startTime;
     * logger.log("✨ Movimiento seleccionado en " + totalTime + "ms");
     * 
     * return bestMove;
     * }
     */
}
