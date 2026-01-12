package chess.controller;

import chess.model.Game;
import chess.model.Board;
import chess.model.Position;
import chess.model.pieces.PieceColor;
import chess.model.pieces.Queen;
import chess.model.player.Player;
import chess.model.player.HumanPlayer;
import chess.model.player.AIPlayer;
import chess.view.GameView;
import javafx.application.Platform;
import java.util.Arrays;
import java.util.List;

/**
 * GameController - Orquesta el flujo del juego
 * 
 * RESPONSABILIDAD:
 * - Recibe clics del tablero desde GameView
 * - Valida movimientos a través de RulesEngine
 * - Actualiza el modelo Game y Board
 * - Ejecuta animaciones a través del BoardAnimationManager
 * - Actualiza la UI del GameView
 */
public class GameController {
    private Game game;
    private GameView gameView;
    private Position selectedPosition; // Pieza seleccionada
    private RulesEngine rulesEngine;

    public GameController() {
        this.rulesEngine = new RulesEngine();
    }

    /**
     * Establece la referencia a GameView para actualizar la UI
     */
    public void setGameView(GameView gameView) {
        this.gameView = gameView;
    }

    public void startNewGame() {
        List<Player> players = Arrays.asList(new HumanPlayer(PieceColor.WHITE), new HumanPlayer(PieceColor.BLACK));
        game = new Game(players, new Board());
        game.startNewGame(players);

        // Actualizar la UI con el nuevo tablero
        initializeGameView();
    }

    public void startNewGameVsAI(PieceColor humanColor, int difficulty) {
        Player humanPlayer = new HumanPlayer(humanColor);
        Player aiPlayer = new AIPlayer(humanColor == PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE,
                difficulty);

        List<Player> players = humanColor == PieceColor.WHITE
                ? Arrays.asList(humanPlayer, aiPlayer)
                : Arrays.asList(aiPlayer, humanPlayer);

        game = new Game(players, new Board());
        game.startNewGame(players);

        // Actualizar la UI con el nuevo tablero
        initializeGameView();
    }

    public void startNewGameAIvsAI(int difficulty1, int difficulty2) {
        Player aiPlayer1 = new AIPlayer(PieceColor.WHITE, difficulty1);
        Player aiPlayer2 = new AIPlayer(PieceColor.BLACK, difficulty2);

        List<Player> players = Arrays.asList(aiPlayer1, aiPlayer2);
        game = new Game(players, new Board());
        game.startNewGame(players);

        // Actualizar la UI con el nuevo tablero
        initializeGameView();
    }

    /**
     * Inicializa la vista del juego con el tablero actual
     */
    private void initializeGameView() {
        if (gameView != null && game != null) {
            // Actualizar el tablero con el estado actual
            gameView.getBoardView().getChessBoard().updateBoard(game.getBoard());

            // Actualizar información del juego
            gameView.getLeftPanel().setTurn("White");
            gameView.getLeftPanel().setGameState("In progress");
        }
    }

    /**
     * Procesa un clic en el tablero
     * Flujo: Seleccionar pieza → mostrar movimientos válidos → ejecutar movimiento
     */
    public void handleSquareClick(Position position) {
        if (game == null || gameView == null) {
            return;
        }

        Board board = game.getBoard();
        chess.model.pieces.Piece clickedPiece = board.getPieceAt(position);

        // Si no hay pieza seleccionada, intentar seleccionar una
        if (selectedPosition == null) {
            // Validar que sea el turno correcto solo al seleccionar pieza propia
            if (clickedPiece != null && clickedPiece.getColor() == game.getCurrentTurn()) {
                selectedPosition = position;

                // Calcular movimientos posibles legales desde RulesEngine
                java.util.List<chess.model.Move> possibleMoves = rulesEngine.getLegalMovesForPiece(position, board);

                // Mostrar indicadores de movimientos posibles
                gameView.getBoardView().getChessBoard().highlightPossibleMoves(position, possibleMoves);
                System.out.println("✓ Pieza seleccionada en: " + position + " (" + clickedPiece.toUnicode() + ") - "
                        + possibleMoves.size() + " movimientos legales");
            }
        } else {
            // Hay una pieza seleccionada
            chess.model.pieces.Piece selectedPiece = board.getPieceAt(selectedPosition.getRow(),
                    selectedPosition.getCol());

            // Si se hace clic en la misma pieza, deseleccionar
            if (position.equals(selectedPosition)) {
                gameView.getBoardView().getChessBoard().clearHighlights();
                selectedPosition = null;
                System.out.println("✗ Deseleccionado");
                return;
            }

            // Si se hace clic en otra pieza del mismo color, cambiar selección
            if (clickedPiece != null && selectedPiece != null &&
                    clickedPiece.getColor() == selectedPiece.getColor() &&
                    clickedPiece.getColor() == game.getCurrentTurn()) {
                gameView.getBoardView().getChessBoard().clearHighlights();
                selectedPosition = position;

                // Calcular y mostrar movimientos legales de la nueva pieza
                java.util.List<chess.model.Move> possibleMoves = rulesEngine.getLegalMovesForPiece(position, board);
                gameView.getBoardView().getChessBoard().highlightPossibleMoves(position, possibleMoves);
                System.out.println("✓ Pieza cambiada a: " + position + " (" + clickedPiece.toUnicode() + ") - "
                        + possibleMoves.size() + " movimientos legales");
                return;
            }

            // Intentar mover la pieza seleccionada a esta posición
            if (selectedPiece == null) {
                gameView.getBoardView().getChessBoard().clearHighlights();
                selectedPosition = null;
                System.out.println("✗ Error: pieza seleccionada no encontrada");
                return;
            }

            chess.model.Move move = new chess.model.Move(selectedPosition, position);

            if (rulesEngine.isValidMove(selectedPosition.getRow(), selectedPosition.getCol(),
                    position.getRow(), position.getCol(), board)) {

                // Verificar que el movimiento no deja el rey en jaque
                if (rulesEngine.leavesKingInCheck(selectedPiece, selectedPosition.getRow(), selectedPosition.getCol(),
                        position.getRow(), position.getCol(), board)) {
                    gameView.getBoardView().getChessBoard().clearHighlights();
                    selectedPosition = null;
                    System.out.println("✗ Movimiento ilegal: deja el rey en jaque");
                    return;
                }

                // Movimiento válido - ejecutar
                // Ejecutar el movimiento (esto actualiza el tablero y maneja casos especiales)
                rulesEngine.executeMove(selectedPosition.getRow(), selectedPosition.getCol(),
                        position.getRow(), position.getCol(), board);

                gameView.getBoardView().getChessBoard().clearHighlights();

                // Animar el movimiento
                gameView.getBoardView().getChessBoard().getAnimationManager().animateMove(move, () -> {
                    // Cuando termina la animación

                    // Limpiar el indicador de última jugada anterior y mostrar el nuevo
                    gameView.getBoardView().getChessBoard().hideLastMoveIndicators();
                    gameView.getBoardView().getChessBoard().showLastMoveIndicators(
                            selectedPosition, position);

                    // Verificar si hay promoción pendiente
                    if (selectedPiece.getType().name().equals("PAWN") &&
                            rulesEngine.isPawnPromotion(position.getRow(), selectedPiece)) {
                        handlePawnPromotion(position);
                    }

                    // Cambiar turno ANTES de actualizar la vista
                    PieceColor nextColor = game.getCurrentTurn() == PieceColor.WHITE ? PieceColor.BLACK
                            : PieceColor.WHITE;
                    game.switchTurn();
                    selectedPosition = null;

                    // Actualizar vista CON EL NUEVO TURNO
                    updateGameViewAfterMove();

                    // Verificar estado del juego
                    checkGameStatus(nextColor, board, selectedPiece.getColor());

                    // Si el siguiente jugador es una IA, ejecutar su turno después de un pequeño
                    // delay
                    if (getPlayerForColor(nextColor) instanceof AIPlayer) {
                        // Ejecutar movimiento de IA después de 1 segundo
                        new java.util.Timer().schedule(new java.util.TimerTask() {
                            @Override
                            public void run() {
                                // Ejecutar en el hilo de FX
                                Platform.runLater(() -> executeAIMove(nextColor));
                            }
                        }, 1000);
                    }
                });

                System.out.println("✓ Movimiento válido: " + selectedPosition + " -> " + position);
            } else {
                // Movimiento no válido - deseleccionar
                gameView.getBoardView().getChessBoard().clearHighlights();
                selectedPosition = null;
                System.out.println("✗ Movimiento inválido: " + selectedPosition + " -> " + position);
            }
        }
    }

    /**
     * Maneja la promoción de un peón (mostrar diálogo para seleccionar pieza)
     */
    private void handlePawnPromotion(Position promotionPosition) {
        System.out.println("♟ Promoción de peón en: " + promotionPosition);
        // TODO: Mostrar diálogo para seleccionar la pieza de promoción (Q, R, B, N)
        // Por ahora, promocionar automáticamente a reina
        Board board = game.getBoard();
        chess.model.pieces.Piece pawn = board.getPieceAt(promotionPosition.getRow(), promotionPosition.getCol());

        if (pawn != null && pawn.getType().name().equals("PAWN")) {
            // Obtener el color del peón
            PieceColor color = pawn.getColor();

            // Crear una reina del mismo color
            chess.model.pieces.Queen queen = new chess.model.pieces.Queen(color);

            // Reemplazar el peón con la reina
            board.setPieceAt(promotionPosition.getRow(), promotionPosition.getCol(), queen);

            // Actualizar vista
            gameView.getBoardView().getChessBoard().updateBoard(board);
            System.out.println("♛ Peón promovido a Reina");
        }
    }

    /**
     * Actualiza la vista después de un movimiento
     */
    private void updateGameViewAfterMove() {
        if (gameView == null || game == null) {
            return;
        }

        // Limpiar los indicadores de jaque y jaque mate antes de actualizar la vista
        gameView.getBoardView().getChessBoard().hideCheckIndicators();
        gameView.getBoardView().getChessBoard().hideCheckmateIndicators();

        // Actualizar tablero visual
        gameView.getBoardView().getChessBoard().updateBoard(game.getBoard());

        // Actualizar información del juego con el turno actual
        String turn = game.getCurrentTurn() == PieceColor.WHITE ? "White" : "Black";
        gameView.getLeftPanel().setTurn(turn);
        gameView.getLeftPanel().setGameState("In progress");
    }

    /**
     * Obtiene el jugador de un color específico
     */
    private Player getPlayerForColor(PieceColor color) {
        List<Player> players = game.getPlayers();
        for (Player player : players) {
            if (player.getColor() == color) {
                return player;
            }
        }
        return null;
    }

    /**
     * Ejecuta un movimiento de la IA para el color especificado
     */
    private void executeAIMove(PieceColor aiColor) {
        if (game == null || gameView == null) {
            return;
        }

        Board board = game.getBoard();
        Player aiPlayer = getPlayerForColor(aiColor);

        if (!(aiPlayer instanceof AIPlayer)) {
            return;
        }

        System.out.println("\n========== TURNO DE LA IA (" + aiColor + ") ==========");

        // Obtener el mejor movimiento según el algoritmo Minimax
        AIPlayer ai = (AIPlayer) aiPlayer;
        chess.model.Move bestMove = ai.chooseMove(board);

        if (bestMove == null) {
            // No hay movimientos válidos - jaque mate o ahogado
            Platform.runLater(() -> {
                String status = rulesEngine.getGameStatus(aiColor, board);
                if (status.equals("CHECKMATE")) {
                    String winnerName = aiColor == PieceColor.WHITE ? "Black" : "White";
                    gameView.getLeftPanel().setGameState("🏁 CHECKMATE - " + winnerName + " wins!");
                    System.out.println("🏁 ¡JAQUE MATE! " + winnerName + " ha ganado (IA derrotada).");
                } else if (status.equals("STALEMATE")) {
                    gameView.getLeftPanel().setGameState("🤝 STALEMATE - Draw!");
                    System.out.println("🤝 ¡TABLAS POR REY AHOGADO!");
                } else if (status.equals("INSUFFICIENT_MATERIAL")) {
                    gameView.getLeftPanel().setGameState("🤝 INSUFFICIENT MATERIAL - Draw!");
                    System.out.println("🤝 ¡TABLAS POR MATERIAL INSUFICIENTE!");
                }
            });
            return;
        }

        // Ejecutar el movimiento
        Position from = bestMove.getFrom();
        Position to = bestMove.getTo();

        chess.model.pieces.Piece aiPiece = board.getPieceAt(from.getRow(), from.getCol());

        // Ejecutar el movimiento en el modelo
        rulesEngine.executeMove(from.getRow(), from.getCol(), to.getRow(), to.getCol(), board);

        // Cambiar turno inmediatamente después de ejecutar el movimiento
        PieceColor opponentColor = aiColor == PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE;
        game.switchTurn();

        // Ejecutar la animación en el hilo de FX
        Platform.runLater(() -> {
            // Animar el movimiento
            gameView.getBoardView().getChessBoard().getAnimationManager().animateMove(bestMove, () -> {
                // Verificar si hay promoción pendiente
                if (aiPiece.getType().name().equals("PAWN") &&
                        rulesEngine.isPawnPromotion(to.getRow(), aiPiece)) {
                    handlePawnPromotion(to);
                }

                // Limpiar el indicador de última jugada anterior y mostrar el nuevo
                gameView.getBoardView().getChessBoard().hideLastMoveIndicators();
                gameView.getBoardView().getChessBoard().showLastMoveIndicators(from, to);

                // Cuando termina la animación
                updateGameViewAfterMove();

                // Verificar estado del juego
                checkGameStatus(opponentColor, board, aiColor);

                // Si el siguiente jugador también es IA, continuar
                if (getPlayerForColor(opponentColor) instanceof AIPlayer) {
                    new java.util.Timer().schedule(new java.util.TimerTask() {
                        @Override
                        public void run() {
                            executeAIMove(opponentColor);
                        }
                    }, 1000);
                }
            });
        });

        System.out.println("========== FIN DEL TURNO DE LA IA ==========\n");
    }

    public Game LoadLastGame() {
        // TODO: Implementar carga del último juego
        return null;
    }

    public Game getGame() {
        return game;
    }

    /**
     * Verifica el estado del juego y actualiza la UI según corresponda
     * Maneja: Jaque Mate, Ahogado, Material Insuficiente, Jaque
     */
    private void checkGameStatus(PieceColor colorToCheck, Board board, PieceColor winnerColor) {
        String status = rulesEngine.getGameStatus(colorToCheck, board);

        switch (status) {
            case "CHECKMATE":
                // Jaque mate - el jugador actual no puede hacer movimientos legales
                Position kingPos = rulesEngine.findKing(colorToCheck, board);
                gameView.getBoardView().getChessBoard().showCheckmateOnKing(kingPos);
                String winnerName = winnerColor == PieceColor.WHITE ? "White" : "Black";
                gameView.getLeftPanel().setGameState("🏁 CHECKMATE - " + winnerName + " wins!");
                System.out.println("🏁 ¡JAQUE MATE! " + winnerName + " ha ganado.");
                break;

            case "STALEMATE":
                // Ahogado (Rey no en jaque, sin movimientos legales) - TABLAS
                Position kingPos2 = rulesEngine.findKing(colorToCheck, board);
                gameView.getBoardView().getChessBoard().hideCheckIndicators();
                gameView.getLeftPanel().setGameState("🤝 STALEMATE - Draw!");
                System.out.println("🤝 ¡TABLAS POR REY AHOGADO!");
                break;

            case "INSUFFICIENT_MATERIAL":
                // Material insuficiente - TABLAS
                gameView.getLeftPanel().setGameState("🤝 INSUFFICIENT MATERIAL - Draw!");
                System.out.println("🤝 ¡TABLAS POR MATERIAL INSUFICIENTE! (Solo dos reyes)");
                break;

            case "CHECK":
                // Jaque - el rey está bajo ataque pero tiene movimientos legales
                kingPos = rulesEngine.findKing(colorToCheck, board);
                gameView.getBoardView().getChessBoard().showCheckOnKing(kingPos);
                gameView.getLeftPanel().setGameState("⚠️ CHECK!");
                System.out.println("⚠️ ¡JAQUE! " + colorToCheck + " está bajo ataque.");
                break;

            case "IN_PROGRESS":
                // El juego continúa sin problemas
                gameView.getLeftPanel().setGameState("In progress");
                break;

            default:
                System.err.println("❌ Estado de juego desconocido: " + status);
        }
    }
}
