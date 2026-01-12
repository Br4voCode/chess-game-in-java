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
     * 
     * @param historyFile ruta del archivo .dat con el historial
     * @param white       jugador blanco
     * @param black       jugador negro
     * @return una partida reconstruida con todos los movimientos aplicados
     */
    public static Game reconstructGameFromHistory(String historyFile, Player white, Player black) {
        Game game = new Game(white, black);
        game.setMoveHistoryPath(historyFile);

        StepHistoryStore store = game.getStepHistoryStore();
        List<Step> steps = store.loadApplied();

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

        game.setShouldSaveMoves(false);

        if (DEBUG) {
            System.out.println("Reconstruyendo partida con " + steps.size() + " movimientos...");
        }
        int movesApplied = 0;

        for (Step step : steps) {
            Move move = step.getMove();

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

        game.setMoveCount(movesApplied);

        game.setTurn((movesApplied % 2 == 0) ? chess.model.PieceColor.WHITE : chess.model.PieceColor.BLACK);

        
        chess.history.GameMetadata metadata = store.getGameMetadata();
        if (metadata != null && (metadata.getWhiteTimeMillis() > 0 || metadata.getBlackTimeMillis() > 0)) {
            game.getGameClock().setTimeRemaining(metadata.getWhiteTimeMillis(), metadata.getBlackTimeMillis());
            game.getGameClock().setActivePlayer(game.getTurn());
        }

        game.setShouldSaveMoves(true);

        if (DEBUG) {
            System.out.println("Partida reconstruida: " + movesApplied + " movimientos aplicados.");
            System.out.println("Listo para continuar la partida.\n");
        }
        return game;
    }

    /**
     * Obtiene información sobre el último juego guardado
     * 
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

    /**
     * Obtiene el modo de juego guardado
     * @param historyFile ruta del archivo de historial
     * @return el modo de juego guardado o null si no existe
     */
    public static chess.history.GameMetadata.GameMode getGameMode(String historyFile) {
        StepHistoryStore store = new StepHistoryStore(historyFile);
        store.loadApplied();
        chess.history.GameMetadata metadata = store.getGameMetadata();
        return metadata != null ? metadata.getGameMode() : null;
    }
}
