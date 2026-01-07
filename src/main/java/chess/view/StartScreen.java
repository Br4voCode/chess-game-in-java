package chess.view;

import java.io.File;

import chess.history.StepHistoryStore;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * شاشة البداية التي تسمح للمستخدم بالاختيار بين:
 * - بدء لعبة جديدة
 * - تحميل آخر لعبة محفوظة
 * - لعبة بين لاعبين
 * - لعبة ذكاء اصطناعي مقابل ذكاء اصطناعي
 */
public class StartScreen {
    private BorderPane root;
    private Runnable onNewGame;
    private Runnable onLoadGame;
    private Runnable onNewTwoPlayerGame;
    private Runnable onNewAIVsAIGame;
    private static final String HISTORY_FILE = "game_history.dat";

    public StartScreen(Runnable onNewGame, Runnable onLoadGame, Runnable onNewTwoPlayerGame) {
        this(onNewGame, onLoadGame, onNewTwoPlayerGame, null);
    }

    public StartScreen(Runnable onNewGame, Runnable onLoadGame, Runnable onNewTwoPlayerGame, Runnable onNewAIVsAIGame) {
        this.onNewGame = onNewGame;
        this.onLoadGame = onLoadGame;
        this.onNewTwoPlayerGame = onNewTwoPlayerGame;
        this.onNewAIVsAIGame = onNewAIVsAIGame;
        initializeComponents();
    }

    private void initializeComponents() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #2b2b2b;");

        VBox mainPanel = createMainPanel();
        root.setCenter(mainPanel);
    }

    private VBox createMainPanel() {
        VBox panel = new VBox(30);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(50));
        panel.setStyle("-fx-background-color: #2b2b2b;");

        Label titleLabel = new Label("♔ CHESS GAME ♚");
        titleLabel.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitleLabel = new Label("Select an option");
        subtitleLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #bbbbbb;");

        Button newGameButton = new Button("1 Player (vs AI)");
        newGameButton.setStyle(
                "-fx-font-size: 18px; " +
                        "-fx-padding: 15px 50px; " +
                        "-fx-background-color: #4caf50; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand;");
        newGameButton.setMinWidth(250);
        newGameButton.setOnAction(e -> onNewGame.run());

        Button newTwoPlayerGameButton = new Button("2 Players");
        newTwoPlayerGameButton.setStyle(
                "-fx-font-size: 18px; " +
                        "-fx-padding: 15px 50px; " +
                        "-fx-background-color: #ff9800; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand;");
        newTwoPlayerGameButton.setMinWidth(250);
        newTwoPlayerGameButton.setOnAction(e -> onNewTwoPlayerGame.run());

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
            aiVsAIButton.setOnAction(e -> onNewAIVsAIGame.run());
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

    public BorderPane getRoot() {
        return root;
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
