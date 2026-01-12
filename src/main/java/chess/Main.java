package chess;

import chess.controller.GameController;
import chess.view.AppView;
import chess.utils.ImageCache;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private AppView appView;
    private GameController gameController;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Cargar todas las imágenes en cache antes de mostrar la interfaz
        ImageCache.initialize();

        // Crear la vista raíz
        appView = new AppView();

        // Crear el controlador
        gameController = new GameController();

        // Conectar el GameView con el GameController
        gameController.setGameView(appView.getGameView());

        appView.getStartScreen().setOnNewTwoPlayerGame(() -> {
            gameController.startNewGame();

            // Conectar clics del tablero con GameController
            appView.getGameView().getBoardView().onSquareClick(position -> {
                gameController.handleSquareClick(position);
            });

            appView.showGameView();
        });

        appView.getStartScreen().setOnLoadGame(() -> {
            gameController.LoadLastGame();

            // Conectar clics del tablero con GameController
            appView.getGameView().getBoardView().onSquareClick(position -> {
                gameController.handleSquareClick(position);
            });

            appView.showGameView();
        });

        // También conectar para el modo 1 Player vs AI
        appView.getStartScreen().setOnNewGameVsAI((colorAndDifficulty) -> {
            Object[] params = (Object[]) colorAndDifficulty;
            if (params[0] == null) {
                // IA vs IA
                gameController.startNewGameAIvsAI((int) params[1], (int) params[1]);
            } else {
                // Humano vs IA
                gameController.startNewGameVsAI((chess.model.pieces.PieceColor) params[0], (int) params[1]);
            }

            // Conectar clics del tablero con GameController
            appView.getGameView().getBoardView().onSquareClick(position -> {
                gameController.handleSquareClick(position);
            });

            appView.showGameView();
        });

        // Conectar botón Back del GameView con StartScreen
        appView.getGameView().setOnBackToMenu(() -> {
            appView.showStartScreen();
        });

        // Mostrar la pantalla de inicio
        appView.showStartScreen();

        // Configurar la ventana
        Scene scene = new Scene(appView.getRoot(), 1200, 800);
        primaryStage.setTitle("Chess Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
