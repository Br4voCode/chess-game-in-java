package chess;

import chess.view.GameView;
import chess.view.PieceImageLoader;
import chess.view.StartScreen;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {
    private StackPane root;
    private StartScreen startScreen;
    private GameView currentGameView;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Chess with JavaFX");
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        PieceImageLoader.preloadAllPieceImages();

        root = new StackPane();
        root.setStyle("-fx-background-color: #2b2b2b;");

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true);

        showStartScreen();

        primaryStage.show();
    }

    private void showStartScreen() {
        startScreen = new StartScreen(
                this::startNewGame,
                this::loadLastGame,
                this::startNewTwoPlayerGame,
                this::startNewAIVsAIGame);
        root.getChildren().clear();
        root.getChildren().add(startScreen.getRoot());
    }

    private void startNewGame() {
        currentGameView = new GameView();
        root.getChildren().clear();
        root.getChildren().add(currentGameView.getRoot());
    }

    private void loadLastGame() {
        currentGameView = new GameView(true);
        root.getChildren().clear();
        root.getChildren().add(currentGameView.getRoot());
    }

    private void startNewTwoPlayerGame() {
        currentGameView = new GameView(false, true);
        root.getChildren().clear();
        root.getChildren().add(currentGameView.getRoot());
    }

    private void startNewAIVsAIGame() {
        currentGameView = new GameView(false, false, true);
        root.getChildren().clear();
        root.getChildren().add(currentGameView.getRoot());
    }

    public static void main(String[] args) {
        launch(args);
    }
}