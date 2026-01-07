package chess.game;

import chess.model.Board;
import chess.model.Move;
import chess.history.Step;
import chess.history.StepHistoryStore;

import java.util.List;

/**
 * Helper class para reconstruir una partida desde el historial guardado.
 */
public class GameReconstructor {

    private static final boolean DEBUG = false;

    /**
     * Reconstruye una partida a partir del historial guardado
     * @param historyFile ruta del archivo .dat con el historial
     * @param white jugador blanco
     * @param black jugador negro
     * @return una partida reconstruida con todos los movimientos aplicados
     */
    public static Game reconstructGameFromHistory(String historyFile, Player white, Player black) {
        Game game = new Game(white, black);
        game.setMoveHistoryPath(historyFile);

        StepHistoryStore store = game.getStepHistoryStore();
        List<Step> steps = store.loadApplied();

        // Cargar el historial en memoria (base para undo/redo). Importante: esto NO debe
        // escribir el archivo; solo inicializa el estado.
        if (game.getStepHistory() != null) {
            game.getStepHistory().loadAppliedSteps(steps);
        }

        if (steps.isEmpty()) {
            if (DEBUG) {
                System.out.println("El historial está vacío. Iniciando nueva partida.");
            }
            game.setShouldSaveMoves(true);
            return game;
        }

        // Desactivar guardado durante la reconstrucción (no queremos reescribir el archivo
        // mientras aplicamos los movimientos ya persistidos)
        game.setShouldSaveMoves(false);

        // Aplicar todos los movimientos al tablero
        if (DEBUG) {
            System.out.println("Reconstruyendo partida con " + steps.size() + " movimientos...");
        }
        int movesApplied = 0;

        for (Step step : steps) {
            Move move = step.getMove();
            // Aplicar el movimiento directamente sin validación
            // (asumimos que el historial contiene solo movimientos válidos)
            Board board = game.getBoard();

            if (board.getPieceAt(move.getFrom()) != null) {
                game.applyMove(move);
                movesApplied++;
            } else {
                if (DEBUG) {
                    System.err.println("Error: No se encontró pieza en " + move.getFrom());
                }
                break;
            }
        }

    // Fijar contadores coherentes con el historial cargado
    game.setMoveCount(movesApplied);
    // Por convenio: si hay número impar de jugadas aplicadas, le toca al negro;
    // si es par, le toca al blanco.
    game.setTurn((movesApplied % 2 == 0) ? chess.model.PieceColor.WHITE : chess.model.PieceColor.BLACK);

    // Reactivar el guardado para nuevos movimientos.
    // NO limpiar el historial: queremos poder seguir jugando y usar undo/redo.
        game.setShouldSaveMoves(true);

        if (DEBUG) {
            System.out.println("Partida reconstruida: " + movesApplied + " movimientos aplicados.");
            System.out.println("Listo para continuar la partida.\n");
        }
        return game;
    }

    /**
     * Obtiene información sobre el último juego guardado
     * @param historyFile ruta del archivo de historial
     * @return descripción del último juego o null si no existe
     */
    public static String getGameInfo(String historyFile) {
        StepHistoryStore store = new StepHistoryStore(historyFile);
        List<Step> steps = store.loadApplied();
        int moveCount = steps.size();
        if (moveCount == 0) {
            return null;
        }
        int fullMoves = (moveCount + 1) / 2;
        String nextColor = (moveCount % 2 == 0) ? "White" : "Black";
        return "Saved game: " + fullMoves + " full moves, " + nextColor + " to move";
    }
}
