package chess;

import chess.game.GameSettings;
import chess.game.GameSettingsStore;
import chess.model.PieceColor;
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

    private void startNewGame(PieceColor humanColor, int aiDepth) {
        GameSettings settings = GameSettings.humanVsAI(humanColor, aiDepth);
        GameSettingsStore.save(settings);
        currentGameView = new GameView(settings);
        root.getChildren().clear();
        root.getChildren().add(currentGameView.getRoot());
    }

    private void loadLastGame() {
        GameSettings settings = GameSettingsStore.loadOrDefault();
        currentGameView = new GameView(true, settings);
        root.getChildren().clear();
        root.getChildren().add(currentGameView.getRoot());
    }

    private void startNewTwoPlayerGame() {
        GameSettings settings = GameSettings.twoPlayers();
        GameSettingsStore.save(settings);
        currentGameView = new GameView(settings);
        root.getChildren().clear();
        root.getChildren().add(currentGameView.getRoot());
    }

    private void startNewAIVsAIGame() {
        GameSettings stored = GameSettingsStore.loadOrDefault();
        GameSettings settings = stored.isAIVsAI()
                ? stored
                : GameSettings.aiVsAi(stored.getAiDepth());
        GameSettingsStore.save(settings);
        currentGameView = new GameView(settings);
        root.getChildren().clear();
        root.getChildren().add(currentGameView.getRoot());
    }

    public static void main(String[] args) {
        launch(args);
    }
}