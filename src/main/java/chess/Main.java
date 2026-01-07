package chess;

import java.util.function.Supplier;

import chess.ai.GameTree;
import chess.ai.MinimaxTreeSearch;
import chess.ai.SimpleEvaluator;
import chess.game.GameSettings;
import chess.game.GameSettingsStore;
import chess.model.Board;
import chess.model.PieceColor;
import chess.view.GameView;
import chess.view.LoadingScreen;
import chess.view.PieceImageLoader;
import chess.view.StartScreen;
import javafx.application.Application;
import javafx.concurrent.Task;
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
        launchWithLoading("Preparando reto vs IA", settings, () -> new GameView(settings));
    }

    private void loadLastGame() {
        GameSettings settings = GameSettingsStore.loadOrDefault();
        launchWithLoading("Cargando partida guardada", settings, () -> new GameView(true, settings));
    }

    private void startNewTwoPlayerGame() {
        GameSettings settings = GameSettings.twoPlayers();
        GameSettingsStore.save(settings);
        launchWithLoading("Configurando duelo local", settings, () -> new GameView(settings));
    }

    private void startNewAIVsAIGame() {
        GameSettings stored = GameSettingsStore.loadOrDefault();
        GameSettings settings = stored.isAIVsAI()
                ? stored
                : GameSettings.aiVsAi(stored.getAiDepth());
        GameSettingsStore.save(settings);
        launchWithLoading("Sincronizando IA vs IA", settings, () -> new GameView(settings));
    }

    private void launchWithLoading(String headline, GameSettings settings, Supplier<GameView> gameFactory) {
        LoadingScreen loadingScreen = new LoadingScreen(headline, settings, this::showStartScreen);
        Task<Void> loadTask = createLoadingTask(settings);
        loadingScreen.bindToTask(loadTask);
        root.getChildren().clear();
        root.getChildren().add(loadingScreen.getRoot());

        loadTask.setOnSucceeded(event -> {
            try {
                currentGameView = gameFactory.get();
                root.getChildren().clear();
                root.getChildren().add(currentGameView.getRoot());
            } catch (Exception ex) {
                loadingScreen.showError(ex.getMessage(), this::showStartScreen);
            }
        });

        loadTask.setOnFailed(event -> {
            Throwable error = loadTask.getException();
            String message = error != null ? error.getMessage() : "Error durante la carga";
            loadingScreen.showError(message, this::showStartScreen);
        });

        loadTask.setOnCancelled(event -> {
            boolean showingStart = startScreen != null && root.getChildren().contains(startScreen.getRoot());
            if (!showingStart) {
                showStartScreen();
            }
        });

        Thread loaderThread = new Thread(loadTask, "chess-loader-thread");
        loaderThread.setDaemon(true);
        loaderThread.start();
    }

    private Task<Void> createLoadingTask(GameSettings settings) {
        final GameSettings effectiveSettings = settings;
        return new Task<Void>() {
            @Override
            protected Void call() {
                updateMessage("Sincronizando preferencias...");
                updateProgress(0.05, 1);

                if (effectiveSettings != null && !effectiveSettings.isPlayerVsPlayer()) {
                    runAIPrefetch(effectiveSettings.getAiDepth());
                } else {
                    runBoardPriming();
                }

                updateMessage("Aplicando estilo visual...");
                updateProgress(0.98, 1);
                updateProgress(1, 1);
                return null;
            }

            private void runAIPrefetch(int depth) {
                int warmDepth = Math.max(2, Math.min(6, depth));
                GameTree tree = new GameTree(new Board(), PieceColor.WHITE);
                SimpleEvaluator evaluator = new SimpleEvaluator();
                for (int layer = 1; layer <= warmDepth; layer++) {
                    if (isCancelled()) {
                        return;
                    }
                    tree.buildToDepth(layer);
                    double progress = 0.05 + 0.80 * (layer / (double) warmDepth);
                    updateMessage("Explorando capa " + layer + " / " + warmDepth);
                    updateProgress(Math.min(progress, 0.9), 1);
                }
                MinimaxTreeSearch search = new MinimaxTreeSearch(
                        tree,
                        (board, perspective) -> evaluator.evaluate(board, perspective),
                        PieceColor.WHITE);
                search.runAndGetBestMove();
                updateMessage("Afinando heurística inicial");
                updateProgress(0.93, 1);
            }

            private void runBoardPriming() {
                Board board = new Board();
                PieceColor current = PieceColor.WHITE;
                for (int cycle = 1; cycle <= 4; cycle++) {
                    if (isCancelled()) {
                        return;
                    }
                    board.getAllPossibleMoves(current);
                    double progress = 0.05 + 0.7 * (cycle / 4.0);
                    String colorLabel = current == PieceColor.WHITE ? "blancas" : "negras";
                    updateMessage("Sincronizando escenarios para " + colorLabel);
                    updateProgress(Math.min(progress, 0.85), 1);
                    current = current.opposite();
                }
            }
        };
    }

    public static void main(String[] args) {
        launch(args);
    }
}