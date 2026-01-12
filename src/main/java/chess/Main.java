package chess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import chess.ai.GameTree;
import chess.ai.GameTreeNode;
import chess.ai.MinimaxTreeSearch;
import chess.ai.SimpleEvaluator;
import chess.game.GameSettings;
import chess.game.GameSettingsStore;
import chess.model.Board;
import chess.model.Move;
import chess.model.PieceColor;
import chess.model.Position;
import chess.view.GameView;
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
        try {
            // Create game view directly without loading screen
            currentGameView = gameFactory.get();
                currentGameView.setOnBackToMenu(this::showStartScreen);
            Task<Void> loadTask = createLoadingTask(settings);
            
            // Get the log console from the game view and bind it to the task
            chess.view.LogConsole logConsole = currentGameView.getLogConsole();
            
            loadTask.setOnSucceeded(event -> {
                // Task completed successfully
                logConsole.log("✅ " + headline + " completado");
            });

            loadTask.setOnFailed(event -> {
                Throwable error = loadTask.getException();
                String message = error != null ? error.getMessage() : "Error durante la carga";
                logConsole.log("❌ Error: " + message);
            });

            loadTask.setOnCancelled(event -> {
                logConsole.log("⚠️ Operación cancelada");
            });

            // Create a custom task that updates log console during execution
            Task<Void> loggingTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    // Capture the original task's title updates as logs
                    loadTask.titleProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal != null && !newVal.isEmpty()) {
                            logConsole.log(newVal);
                        }
                    });
                    
                    loadTask.messageProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal != null && !newVal.isEmpty()) {
                            logConsole.log(newVal);
                        }
                    });
                    
                    // Run the original task
                    loadTask.run();
                    return null;
                }
            };

            root.getChildren().clear();
            root.getChildren().add(currentGameView.getRoot());
            
            logConsole.log("🚀 " + headline + "...");

            Thread loaderThread = new Thread(loggingTask, "chess-loader-thread");
            loaderThread.setDaemon(true);
            loaderThread.start();
        } catch (Exception ex) {
            javafx.application.Platform.runLater(this::showStartScreen);
        }
    }

    private Task<Void> createLoadingTask(GameSettings settings) {
        final GameSettings effectiveSettings = settings;
        return new Task<Void>() {
            private long lastTraceUpdateMs = 0L;

            @Override
            protected Void call() {
                updateMessage("Sincronizando preferencias...");
                updateProgress(0.05, 1);

                if (effectiveSettings != null && !effectiveSettings.isPlayerVsPlayer()) {
                    runAIPrefetch(effectiveSettings.getEngineDepth());
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
                
                updateTitle("Iniciando construcción del árbol de búsqueda (profundidad máxima: " + warmDepth + ")");
                
                for (int layer = 1; layer <= warmDepth; layer++) {
                    if (isCancelled()) {
                        return;
                    }
                    final int targetDepth = layer;
                    final int[] nodeCount = {0};
                    final long startTime = System.currentTimeMillis();
                    final StringBuilder batch = new StringBuilder(4096);
                    final int[] batchLines = {0};
                    final long[] lastFlushNs = {System.nanoTime()};
                    final int maxBurstLines = 5000;
                    
                    tree.buildToDepth(targetDepth, node -> {
                        if (isCancelled()) {
                            return;
                        }
                        nodeCount[0]++;

                        // Generar logs durante el recorrido (miles de líneas), pero enviarlos en batches.
                        if (batchLines[0] < maxBurstLines) {
                            String branch = (node.getDepth() == 0) ? "root" : describeBranch(node, 3);
                            String move = (node.getMoveFromParent() == null) ? "-" : renderMove(node.getMoveFromParent());
                            Integer eval = node.getEvaluation();
                            int children = node.getChildren().size();
                            batch.append("d")
                                    .append(node.getDepth())
                                    .append("/")
                                    .append(targetDepth)
                                    .append(" #")
                                    .append(nodeCount[0])
                                    .append(" side=")
                                    .append(node.getSideToMove())
                                    .append(" move=")
                                    .append(move)
                                    .append(" children=")
                                    .append(children)
                                    .append(" eval=")
                                    .append(eval == null ? "?" : eval)
                                    .append(" | ")
                                    .append(branch)
                                    .append("\n");
                            batchLines[0]++;

                            long nowNs = System.nanoTime();
                            // Flush cada ~30ms o cada 50 líneas para que se vea "rápido".
                            if (batchLines[0] % 50 == 0 || (nowNs - lastFlushNs[0]) > 30_000_000L) {
                                updateTitle(batch.toString());
                                batch.setLength(0);
                                lastFlushNs[0] = nowNs;
                            }
                        }
                    });

                    if (!batch.isEmpty()) {
                        updateTitle(batch.toString());
                    }
                    
                    long elapsed = System.currentTimeMillis() - startTime;
                    updateTitle("Capa " + layer + " completada: " + nodeCount[0] + " nodos en " + elapsed + "ms");
                    
                    double progress = 0.05 + 0.80 * (layer / (double) warmDepth);
                    updateMessage("Explorando capa " + layer + " / " + warmDepth);
                    updateProgress(Math.min(progress, 0.9), 1);
                }
                
                updateTitle("Propagando evaluaciones minimax...");
                MinimaxTreeSearch search = new MinimaxTreeSearch(
                        tree,
                        (board, perspective) -> evaluator.evaluate(board, perspective),
                        PieceColor.WHITE);
                search.runAndGetBestMove();
                updateTitle("Rama representativa establecida");
                updateMessage("Afinando heurística inicial");
                updateProgress(0.93, 1);
            }

            private void runBoardPriming() {
                Board board = new Board();
                PieceColor current = PieceColor.WHITE;
                
                updateTitle("🎯 Inicializando motor de sincronización del tablero");
                
                for (int cycle = 1; cycle <= 4; cycle++) {
                    if (isCancelled()) {
                        return;
                    }
                    long startTime = System.currentTimeMillis();
                    int moveCount = board.getAllPossibleMoves(current).size();
                    long elapsed = System.currentTimeMillis() - startTime;
                    
                    String colorLabel = current == PieceColor.WHITE ? "⚪ Blancas" : "⚫ Negras";
                    updateTitle(colorLabel + ": " + moveCount + " movimientos posibles (" + elapsed + "ms)");
                    
                    double progress = 0.05 + 0.7 * (cycle / 4.0);
                    updateMessage("Sincronizando escenarios para " + (current == PieceColor.WHITE ? "blancas" : "negras"));
                    updateProgress(Math.min(progress, 0.85), 1);
                    current = current.opposite();
                }
                
                updateTitle("✅ Tablero sincronizado y listo");
            }

            private void pushTreeTrace(GameTreeNode node) {
                if (node == null || node.getDepth() == 0) {
                    return;
                }
                long now = System.currentTimeMillis();
                if (now - lastTraceUpdateMs < 20) {
                    return;
                }
                lastTraceUpdateMs = now;
                String branch = describeBranch(node, 4);
                int depth = node.getDepth();
                int childCount = node.getChildren().size();
                updateTitle("🔗 [Profundidad " + depth + "] " + branch + " | hijos: " + childCount);
            }

            private String describeBranch(GameTreeNode node, int maxMoves) {
                List<String> segments = new ArrayList<>();
                GameTreeNode cursor = node;
                while (cursor != null && cursor.getMoveFromParent() != null && segments.size() < maxMoves) {
                    segments.add(renderMove(cursor.getMoveFromParent()));
                    cursor = cursor.getParent();
                }
                if (segments.isEmpty()) {
                    return "origen";
                }
                Collections.reverse(segments);
                return String.join(" · ", segments);
            }

            private String renderMove(Move move) {
                if (move == null) {
                    return "--";
                }
                String base = toAlgebraic(move.getFrom()) + "->" + toAlgebraic(move.getTo());
                if (move.getPromotion() != null) {
                    base += "=" + move.getPromotion().getType();
                }
                return base;
            }

            private String toAlgebraic(Position position) {
                if (position == null) {
                    return "??";
                }
                char file = (char) ('a' + position.getCol());
                int rank = 8 - position.getRow();
                return String.valueOf(file) + rank;
            }
        };
    }

    public static void main(String[] args) {
        launch(args);
    }
}