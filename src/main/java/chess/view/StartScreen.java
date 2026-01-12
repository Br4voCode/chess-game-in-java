package chess.view;

import java.io.File;
import java.util.function.Consumer;

import chess.model.pieces.PieceColor;
import chess.view.components.StartMenuButton;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class StartScreen {
    private StackPane mainContainer;
    private Consumer<Object> onNewGameVsAI;
    private Runnable onLoadGame;
    private Runnable onNewTwoPlayerGame;
    private static final String HISTORY_FILE = "game_history.dat";

    public StartScreen() {
        initializeComponents();
    }

    private void initializeComponents() {
        VBox mainPanel = createMainPanel();

        mainContainer = new StackPane();
        mainContainer.setStyle("-fx-background-color: linear-gradient(to bottom right, #0f0f10, #2b2b2b);");
        mainContainer.getChildren().add(mainPanel);
    }

    private VBox createMainPanel() {
        VBox panel = new VBox(30);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(60));
        panel.setStyle(
                "-fx-background-color: rgba(33,33,35,0.95);" +
                        "-fx-background-radius: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.55), 35, 0, 0, 18);");

        // Título
        Label titleLabel = new Label("♔ CHESS GAME ♚");
        titleLabel.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitleLabel = new Label("Choose game mode:");
        subtitleLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #9e9e9e;");

        // Configuración de botones
        double buttonMinWidth = 250;

        // Crear botones usando el componente StartMenuButton con colores hex
        StartMenuButton newGameButton = new StartMenuButton("1 Player (vs AI)", "#4caf50", buttonMinWidth, true);
        newGameButton.setOnAction(e -> {
            if (onNewGameVsAI != null) {
                // Pasar PieceColor.WHITE y dificultad 3 por defecto
                onNewGameVsAI.accept(new Object[] { PieceColor.WHITE, 3 });
            }
        });

        StartMenuButton twoPlayerButton = new StartMenuButton("2 Players", "#ff9800", buttonMinWidth, true);
        twoPlayerButton.setOnAction(e -> {
            if (onNewTwoPlayerGame != null) {
                onNewTwoPlayerGame.run();
            }
        });

        StartMenuButton aiVsAiButton = new StartMenuButton("AI vs AI", "#9c27b0", buttonMinWidth, true);
        aiVsAiButton.setOnAction(e -> {
            if (onNewGameVsAI != null) {
                // Pasar null para indicar que es IA vs IA
                onNewGameVsAI.accept(new Object[] { null, 3 });
            }
        });

        boolean hasHistory = hasGameHistory();
        StartMenuButton loadGameButton = new StartMenuButton("Load Game", "#2196f3", buttonMinWidth, hasHistory);
        if (hasHistory) {
            loadGameButton.setOnAction(e -> {
                if (onLoadGame != null) {
                    onLoadGame.run();
                }
            });
        }

        panel.getChildren().addAll(titleLabel, subtitleLabel, newGameButton, twoPlayerButton, aiVsAiButton,
                loadGameButton);
        return panel;
    }

    // ==================== SETTERS PARA CALLBACKS ====================

    public void setOnNewGameVsAI(Consumer<Object> callback) {
        this.onNewGameVsAI = callback;
    }

    public void setOnNewTwoPlayerGame(Runnable callback) {
        this.onNewTwoPlayerGame = callback;
    }

    public void setOnLoadGame(Runnable callback) {
        this.onLoadGame = callback;
    }

    public StackPane getMainContainer() {
        return mainContainer;
    }

    public StackPane getRoot() {
        return mainContainer;
    }

    public static String getHistoryFilePath() {
        return HISTORY_FILE;
    }

    public static boolean hasGameHistory() {
        return new File(HISTORY_FILE).exists();
    }
}
