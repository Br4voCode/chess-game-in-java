package chess.game;

import java.util.List;

import chess.model.Board;
import chess.model.Move;
import chess.model.MoveHistory;

/**
 * Helper class para reconstruir una partida desde el historial guardado.
 */
public class GameReconstructor {

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

        // Cargar el historial desde el archivo
        if (!game.getMoveHistory().loadFromFile()) {
            System.out.println("No se pudo cargar el historial. Iniciando nueva partida.");
            game.setShouldSaveMoves(true);
            return game;
        }

        // Obtener todos los movimientos
        List<Move> moves = game.getMoveHistory().getMoves();

        if (moves.isEmpty()) {
            System.out.println("El historial está vacío. Iniciando nueva partida.");
            game.setShouldSaveMoves(true);
            return game;
        }

        // Desactivar guardado durante la reconstrucción
        game.setShouldSaveMoves(false);

        // Aplicar todos los movimientos al tablero
        System.out.println("Reconstruyendo partida con " + moves.size() + " movimientos...");
        int movesApplied = 0;

        for (Move move : moves) {
            // Aplicar el movimiento directamente sin validación
            // (asumimos que el historial contiene solo movimientos válidos)
            Board board = game.getBoard();

            if (board.getPieceAt(move.getFrom()) != null) {
                board.movePiece(move);
                game.applyMove(move);
                movesApplied++;
            } else {
                System.err.println("Error: No se encontró pieza en " + move.getFrom());
                break;
            }
        }

        // Reactivar el guardado para nuevos movimientos
        // NO limpiar el historial aquí, queremos que los nuevos movimientos se agreguen
        game.setShouldSaveMoves(true);

        System.out.println("Partida reconstruida: " + movesApplied + " movimientos aplicados.");
        System.out.println("Listo para continuar la partida.\n");
        return game;
    }

    /**
     * Obtiene información sobre el último juego guardado
     * @param historyFile ruta del archivo de historial
     * @return descripción del último juego o null si no existe
     */
    public static String getGameInfo(String historyFile) {
        MoveHistory history = new MoveHistory(historyFile);
        if (history.loadFromFile()) {
            int moveCount = history.getMoveCount();
            int fullMoves = (moveCount + 1) / 2;
            String nextColor = (moveCount % 2 == 0) ? "White" : "Black";
            return "Saved game: " + fullMoves + " full moves, " + nextColor + " to move";
        }
        return null;
    }
}
