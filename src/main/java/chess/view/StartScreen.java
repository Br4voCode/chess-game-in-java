package chess.view;

import java.io.File;
import java.util.function.BiConsumer;

import chess.game.GameSettings;
import chess.game.GameSettingsStore;
import chess.history.StepHistoryStore;
import chess.model.PieceColor;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;

/**
 * شاشة البداية التي تسمح للمستخدم بالاختيار بين:
 * - بدء لعبة جديدة
 * - تحميل آخر لعبة محفوظة
 * - لعبة بين لاعبين
 * - لعبة ذكاء اصطناعي مقابل ذكاء اصطناعي
 */
public class StartScreen {
    private BorderPane root;
    private StackPane mainContainer;
    private BiConsumer<PieceColor, Integer> onNewGame;
    private Runnable onLoadGame;
    private Runnable onNewTwoPlayerGame;
    private Runnable onNewAIVsAIGame;
    private static final String HISTORY_FILE = "game_history.dat";
    private GameOptionsDialog currentDialog;

    public StartScreen(BiConsumer<PieceColor, Integer> onNewGame, Runnable onLoadGame, Runnable onNewTwoPlayerGame) {
        this(onNewGame, onLoadGame, onNewTwoPlayerGame, null);
    }

    public StartScreen(BiConsumer<PieceColor, Integer> onNewGame, Runnable onLoadGame, Runnable onNewTwoPlayerGame,
            Runnable onNewAIVsAIGame) {
        this.onNewGame = onNewGame;
        this.onLoadGame = onLoadGame;
        this.onNewTwoPlayerGame = onNewTwoPlayerGame;
        this.onNewAIVsAIGame = onNewAIVsAIGame;
        loadPersistedSettings();
        initializeComponents();
    }

    private void initializeComponents() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #0f0f10, #2b2b2b); -fx-padding: 40;");

        VBox mainPanel = createMainPanel();
        
        
        mainContainer = new StackPane();
        mainContainer.getChildren().add(root);
        root.setCenter(mainPanel);
    }

    private VBox createMainPanel() {
        VBox panel = new VBox(30);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(60));
        panel.setStyle(
                "-fx-background-color: rgba(33,33,35,0.95);" +
                        "-fx-background-radius: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.55), 35, 0, 0, 18);"
        );

        Label titleLabel = new Label("♔ CHESS GAME ♚");
        titleLabel.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitleLabel = new Label("Elige un modo de juego");
        subtitleLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #9e9e9e;");

        Button newGameButton = new Button("1 Player (vs AI)");
        newGameButton.setStyle(
                "-fx-font-size: 18px; " +
                        "-fx-padding: 15px 50px; " +
                        "-fx-background-color: #4caf50; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand;");
        newGameButton.setMinWidth(250);
        newGameButton.setOnAction(e -> showGameOptionsDialog("humanVsAI", (color, difficulty) -> {
            GameSettingsStore.save(GameSettings.humanVsAI(color, difficulty));
            if (onNewGame != null) {
                onNewGame.accept(color, difficulty);
            }
        }));

        Button newTwoPlayerGameButton = new Button("2 Players");
        newTwoPlayerGameButton.setStyle(
                "-fx-font-size: 18px; " +
                        "-fx-padding: 15px 50px; " +
                        "-fx-background-color: #ff9800; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand;");
        newTwoPlayerGameButton.setMinWidth(250);
        newTwoPlayerGameButton.setOnAction(e -> {
            GameSettingsStore.save(GameSettings.twoPlayers());
            onNewTwoPlayerGame.run();
        });

        Button aiVsAIButton = new Button("AI vs AI");
        aiVsAIButton.setStyle(
                "-fx-font-size: 18px; " +
                        "-fx-padding: 15px 50px; " +
                        "-fx-background-color: #9c27b0; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand;");
        aiVsAIButton.setMinWidth(250);
        if (onNewAIVsAIGame != null) {
            aiVsAIButton.setOnAction(e -> showGameOptionsDialog("aiVsAI", (color, difficulty) -> {
                GameSettingsStore.save(GameSettings.aiVsAi(difficulty));
                onNewAIVsAIGame.run();
            }));
        } else {
            aiVsAIButton.setDisable(true);
        }

        Button loadGameButton = new Button("Load Last Game");
        loadGameButton.setStyle(
                "-fx-font-size: 18px; " +
                        "-fx-padding: 15px 50px; " +
                        "-fx-background-color: #2196f3; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand;");
        loadGameButton.setMinWidth(250);

        boolean gameExists = new File(HISTORY_FILE).exists();
        if (!gameExists) {
            loadGameButton.setDisable(true);
            loadGameButton.setStyle(
                    "-fx-font-size: 18px; " +
                            "-fx-padding: 15px 50px; " +
                            "-fx-background-color: #888888; " +
                            "-fx-text-fill: #cccccc; " +
                            "-fx-font-weight: bold; " +
                            "-fx-cursor: default;");
        }

        loadGameButton.setOnAction(e -> {
            if (gameExists) {
                onLoadGame.run();
            }
        });

        Label infoLabel = new Label();
        infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #888888; -fx-wrap-text: true;");
        if (gameExists) {
            StepHistoryStore store = new StepHistoryStore(HISTORY_FILE);
            int moveCount = store.loadApplied().size();
            infoLabel.setText("Found a saved game with " + moveCount + " moves");
        } else {
            infoLabel.setText("No saved game found");
        }

        panel.getChildren().addAll(
                titleLabel,
                subtitleLabel,
                newGameButton,
                newTwoPlayerGameButton,
                aiVsAIButton,
                loadGameButton,
                infoLabel);

        return panel;
    }

    private void loadPersistedSettings() {
        
    }

    private void showGameOptionsDialog(String gameMode, BiConsumer<PieceColor, Integer> callback) {
        currentDialog = new GameOptionsDialog(gameMode);
        mainContainer.getChildren().add(currentDialog.getContainer());

        currentDialog.showDialog(
            () -> {
                
                callback.accept(currentDialog.getSelectedColor(), currentDialog.getSelectedDifficulty());
                mainContainer.getChildren().remove(currentDialog.getContainer());
                currentDialog = null;
            },
            () -> {
                
                mainContainer.getChildren().remove(currentDialog.getContainer());
                currentDialog = null;
            }
        );
    }

    public StackPane getMainContainer() {
        return mainContainer;
    }

    public StackPane getRoot() {
        return mainContainer;
    }

    /**
     * Obtiene la ruta del archivo de historial
     */
    public static String getHistoryFilePath() {
        return HISTORY_FILE;
    }

    /**
     * Verifica si existe un archivo de historial guardado
     */
    public static boolean hasGameHistory() {
        return new File(HISTORY_FILE).exists();
    }
}
