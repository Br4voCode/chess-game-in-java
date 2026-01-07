package chess;

import chess.view.GameView;
import chess.view.StartScreen;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Chess with JavaFX");
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        // Start in fullscreen
        primaryStage.setFullScreen(true);

        // Mostrar pantalla de inicio
        showStartScreen();

        primaryStage.show();
    }

    private void showStartScreen() {
        StartScreen startScreen = new StartScreen(
                this::startNewGame,
                this::loadLastGame
        );
        Scene scene = new Scene(startScreen.getRoot(), 800, 600);
        primaryStage.setScene(scene);
    }

    private void startNewGame() {
        GameView gameView = new GameView();
        Scene scene = new Scene(gameView.getRoot());
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true);
    }

    private void loadLastGame() {
        GameView gameView = new GameView(true);
        Scene scene = new Scene(gameView.getRoot());
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true);
    }

    public static void main(String[] args) {
        launch(args);
    }
}