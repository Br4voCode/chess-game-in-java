package chess;

import chess.view.GameView;
import chess.view.StartScreen;
import chess.view.TwoPlayerGameView;
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

        // Mostrar pantalla de inicio
        showStartScreen();

        primaryStage.show();
    }

    private void showStartScreen() {
        StartScreen startScreen = new StartScreen(
                this::startNewGame,
                this::loadLastGame,
                this::startTwoPlayerGame
        );
        Scene scene = new Scene(startScreen.getRoot(), 800, 600);
        primaryStage.setScene(scene);
    }

    private void startNewGame() {
        GameView gameView = new GameView();
        Scene scene = new Scene(gameView.getRoot(), 800, 600);
        primaryStage.setScene(scene);
    }

    private void loadLastGame() {
        GameView gameView = new GameView(true);
        Scene scene = new Scene(gameView.getRoot(), 800, 600);
        primaryStage.setScene(scene);
    }

    private void startTwoPlayerGame() {
        TwoPlayerGameView twoPlayerView = new TwoPlayerGameView(this::showStartScreen);
        Scene scene = new Scene(twoPlayerView.getRoot(), 800, 600);
        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}