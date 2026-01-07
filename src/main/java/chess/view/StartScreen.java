package chess.view;

import java.io.File;

import chess.model.MoveHistory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * Pantalla de inicio que permite al usuario elegir entre:
 * - Iniciar una nueva partida
 * - Cargar la última partida guardada
 */
public class StartScreen {
    private BorderPane root;
    private Runnable onNewGame;
    private Runnable onLoadGame;
    private static final String HISTORY_FILE = "game_history.dat";

    public StartScreen(Runnable onNewGame, Runnable onLoadGame) {
        this.onNewGame = onNewGame;
        this.onLoadGame = onLoadGame;
        initializeComponents();
    }

    private void initializeComponents() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #2b2b2b;");

        // Panel central con opciones
        VBox mainPanel = createMainPanel();
        root.setCenter(mainPanel);
    }

    private VBox createMainPanel() {
        VBox panel = new VBox(30);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(50));
        panel.setStyle("-fx-background-color: #2b2b2b;");

        // Título
        Label titleLabel = new Label("♔ CHESS GAME ♚");
        titleLabel.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Subtítulo
        Label subtitleLabel = new Label("Select an option");
        subtitleLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #bbbbbb;");

        // Botón Nueva Partida
        Button newGameButton = new Button("New Game");
        newGameButton.setStyle(
                "-fx-font-size: 18px; " +
                "-fx-padding: 15px 50px; " +
                "-fx-background-color: #4caf50; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-cursor: hand;"
        );
        newGameButton.setMinWidth(250);
        newGameButton.setOnAction(e -> onNewGame.run());

        // Botón Cargar Partida (deshabilitado si no existe archivo)
        Button loadGameButton = new Button("Load Last Game");
        loadGameButton.setStyle(
                "-fx-font-size: 18px; " +
                "-fx-padding: 15px 50px; " +
                "-fx-background-color: #2196f3; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-cursor: hand;"
        );
        loadGameButton.setMinWidth(250);

        // Verificar si existe archivo de historial
        boolean gameExists = new File(HISTORY_FILE).exists();
        if (!gameExists) {
            loadGameButton.setDisable(true);
            loadGameButton.setStyle(
                    "-fx-font-size: 18px; " +
                    "-fx-padding: 15px 50px; " +
                    "-fx-background-color: #888888; " +
                    "-fx-text-fill: #cccccc; " +
                    "-fx-font-weight: bold; " +
                    "-fx-cursor: default;"
            );
        }

        loadGameButton.setOnAction(e -> {
            if (gameExists) {
                onLoadGame.run();
            }
        });

        // Información
        Label infoLabel = new Label();
        infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #888888; -fx-wrap-text: true;");
        if (gameExists) {
            MoveHistory history = new MoveHistory(HISTORY_FILE);
            if (history.loadFromFile()) {
                infoLabel.setText("Found a saved game with " + history.getMoveCount() + " moves");
            }
        } else {
            infoLabel.setText("No saved game found");
        }

        panel.getChildren().addAll(
                titleLabel,
                subtitleLabel,
                newGameButton,
                loadGameButton,
                infoLabel
        );

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
