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

/**
 * شاشة البداية التي تسمح للمستخدم بالاختيار بين:
 * - بدء لعبة جديدة
 * - تحميل آخر لعبة محفوظة
 * - لعبة بين لاعبين
 * - لعبة ذكاء اصطناعي مقابل ذكاء اصطناعي
 */
public class StartScreen {
    private BorderPane root;
    private BiConsumer<PieceColor, Integer> onNewGame;
    private Runnable onLoadGame;
    private Runnable onNewTwoPlayerGame;
    private Runnable onNewAIVsAIGame;
    private static final String HISTORY_FILE = "game_history.dat";
    private PieceColor selectedPlayerColor = PieceColor.WHITE;
    private int selectedDifficulty = 3;
    private GameSettings lastSettings;

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

        Label subtitleLabel = new Label("Personaliza tu partida y elige un modo");
        subtitleLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #9e9e9e;");

        VBox setupCard = createSetupCard();

        Button newGameButton = new Button("1 Player (vs AI)");
        newGameButton.setStyle(
                "-fx-font-size: 18px; " +
                        "-fx-padding: 15px 50px; " +
                        "-fx-background-color: #4caf50; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand;");
        newGameButton.setMinWidth(250);
        newGameButton.setOnAction(e -> {
            GameSettingsStore.save(GameSettings.humanVsAI(selectedPlayerColor, selectedDifficulty));
            if (onNewGame != null) {
                onNewGame.accept(selectedPlayerColor, selectedDifficulty);
            }
        });

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
            aiVsAIButton.setOnAction(e -> {
                GameSettingsStore.save(GameSettings.aiVsAi(selectedDifficulty));
                onNewAIVsAIGame.run();
            });
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
                setupCard,
                newGameButton,
                newTwoPlayerGameButton,
                aiVsAIButton,
                loadGameButton,
                infoLabel);

        return panel;
    }

    private VBox createSetupCard() {
        VBox card = new VBox(18);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(25));
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.04);" +
                        "-fx-border-color: rgba(255,255,255,0.12);" +
                        "-fx-border-radius: 18;" +
                        "-fx-background-radius: 18;"
        );

        Label setupTitle = new Label("Configuración rápida");
        setupTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label colorLabel = new Label("Color de piezas");
        colorLabel.setStyle("-fx-text-fill: #bdbdbd; -fx-font-size: 14px;");

        ToggleGroup colorGroup = new ToggleGroup();
        ToggleButton whiteToggle = createColorToggle("Jugar con blancas", PieceColor.WHITE, colorGroup);
        ToggleButton blackToggle = createColorToggle("Jugar con negras", PieceColor.BLACK, colorGroup);
        if (selectedPlayerColor == PieceColor.WHITE) {
            whiteToggle.setSelected(true);
        } else {
            blackToggle.setSelected(true);
        }

        HBox colorBox = new HBox(10, whiteToggle, blackToggle);
        colorBox.setAlignment(Pos.CENTER);

        Label difficultyLabel = new Label("Dificultad del AI");
        difficultyLabel.setStyle("-fx-text-fill: #bdbdbd; -fx-font-size: 14px;");

        Label difficultyValueLabel = new Label(formatDifficultyLabel(selectedDifficulty));
        difficultyValueLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        Slider difficultySlider = new Slider(1, 8, selectedDifficulty);
        difficultySlider.setMajorTickUnit(1);
        difficultySlider.setMinorTickCount(0);
        difficultySlider.setShowTickMarks(true);
        difficultySlider.setShowTickLabels(true);
        difficultySlider.setSnapToTicks(true);
        difficultySlider.setPrefWidth(320);
        difficultySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            selectedDifficulty = newVal.intValue();
            difficultyValueLabel.setText(formatDifficultyLabel(selectedDifficulty));
        });

        Label difficultyHint = new Label("Controla la profundidad del árbol de Minimax (1 = rápido, 8 = maestro).");
        difficultyHint.setStyle("-fx-text-fill: #8f8f8f; -fx-font-size: 12px; -fx-wrap-text: true;");

        card.getChildren().addAll(
                setupTitle,
                colorLabel,
                colorBox,
                difficultyLabel,
                difficultyValueLabel,
                difficultySlider,
                difficultyHint
        );

        return card;
    }

    private ToggleButton createColorToggle(String text, PieceColor color, ToggleGroup group) {
        ToggleButton toggle = new ToggleButton(text);
        toggle.setToggleGroup(group);
        toggle.setPrefWidth(160);
        final String baseStyle = "-fx-font-size: 14px;" +
                "-fx-background-color: rgba(255,255,255,0.08);" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 12;" +
                "-fx-padding: 12 20;";
        final String hoverStyle = "-fx-font-size: 14px;" +
                "-fx-background-color: rgba(255,255,255,0.18);" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 12;" +
                "-fx-padding: 12 20;";
        final String selectedStyle = "-fx-font-size: 14px;" +
                "-fx-background-color: linear-gradient(to right, #4caf50, #2e7d32);" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 12;" +
                "-fx-padding: 12 20;" +
                "-fx-effect: dropshadow(gaussian, rgba(76,175,80,0.35), 20, 0, 0, 6);";

        toggle.setStyle(baseStyle);

        toggle.hoverProperty().addListener((obs, wasHover, isHover) -> {
            if (toggle.isSelected()) {
                toggle.setStyle(selectedStyle);
            } else {
                toggle.setStyle(isHover ? hoverStyle : baseStyle);
            }
        });

        toggle.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                selectedPlayerColor = color;
                toggle.setStyle(selectedStyle);
            } else if (!toggle.isHover()) {
                toggle.setStyle(baseStyle);
            }
        });

        toggle.setTooltip(new Tooltip(color == PieceColor.WHITE
                ? "Las blancas mueven primero."
                : "Controla las negras y deja que la IA abra la partida."));

        return toggle;
    }

    private String formatDifficultyLabel(int depth) {
        String descriptor;
        if (depth <= 2) {
            descriptor = "Relajado";
        } else if (depth <= 4) {
            descriptor = "Equilibrado";
        } else if (depth <= 6) {
            descriptor = "Competitivo";
        } else {
            descriptor = "Gran Maestro";
        }
        return "Profundidad " + depth + " · " + descriptor;
    }

    private void loadPersistedSettings() {
        lastSettings = GameSettingsStore.load();
        if (lastSettings != null && lastSettings.isHumanVsAI()) {
            selectedPlayerColor = lastSettings.getHumanColor();
            selectedDifficulty = lastSettings.getAiDepth();
        }
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
