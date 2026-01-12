package chess.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import chess.view.components.LogConsole;

public class LeftPanelView {

    private VBox root;

    private Button undo;
    private Button redo;
    private Button hint;
    private Button newGame;
    private Button back;

    private Label turnLabel;
    private Label turnValue;
    private Label gameStateLabel;
    private Label gameStateValue;

    private LogConsole logConsole;

    public LeftPanelView() {
        build();
    }

    private void build() {
        root = new VBox(10);
        root.setPrefWidth(180);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #3c3f41; -fx-background-radius: 5;");
        root.setMaxHeight(Double.MAX_VALUE);

        Label panelTitle = new Label("Game Controls");
        panelTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        newGame = new Button("New Game");
        newGame.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold;");
        newGame.setMaxWidth(Double.MAX_VALUE);

        // Navigation buttons (undo/redo)
        HBox navBox = new HBox(10);
        navBox.setAlignment(Pos.CENTER);

        undo = new Button("←");
        undo.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-font-weight: bold;");
        undo.setDisable(true);

        redo = new Button("→");
        redo.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-font-weight: bold;");
        redo.setDisable(true);

        navBox.getChildren().addAll(undo, redo);

        hint = new Button("Hint");
        hint.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-font-weight: bold;");
        hint.setMaxWidth(Double.MAX_VALUE);

        // Turn display
        turnLabel = new Label("Current turn:");
        turnLabel.setStyle("-fx-text-fill: #bbbbbb;");

        turnValue = new Label("White");
        turnValue.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Logs
        Label logsTitle = new Label("Logs:");
        logsTitle.setStyle("-fx-text-fill: #bbbbbb;");

        logConsole = new LogConsole();

        VBox logsContainer = new VBox(5);
        logsContainer.setStyle("-fx-background-color: #2b2b2b;");
        logsContainer.getChildren().add(logConsole.getLogsArea());
        VBox.setVgrow(logConsole.getLogsArea(), Priority.ALWAYS);
        VBox.setVgrow(logsContainer, Priority.ALWAYS);

        // Game state
        gameStateLabel = new Label("Game state:");
        gameStateLabel.setStyle("-fx-text-fill: #bbbbbb;");

        gameStateValue = new Label("In progress");
        gameStateValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #4caf50;");

        back = new Button("Back to Menu");
        back.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
        back.setMaxWidth(Double.MAX_VALUE);

        // Spacer para empujar botones hacia abajo
        Pane spacer = new Pane();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        root.getChildren().addAll(
                panelTitle,
                newGame,
                navBox,
                hint,
                new Pane(),
                turnLabel, turnValue,
                logsTitle, logsContainer,
                spacer,
                gameStateLabel, gameStateValue,
                back);
    }

    public VBox getRoot() {
        return root;
    }

    // ==== Eventos ====
    public void onUndo(Runnable r) {
        undo.setOnAction(e -> r.run());
    }

    public void onRedo(Runnable r) {
        redo.setOnAction(e -> r.run());
    }

    public void onHint(Runnable r) {
        hint.setOnAction(e -> r.run());
    }

    public void onNewGame(Runnable r) {
        newGame.setOnAction(e -> r.run());
    }

    public void onBack(Runnable r) {
        back.setOnAction(e -> r.run());
    }

    // ==== Actualizaciones UI ====
    public void setTurn(String text) {
        turnValue.setText(text);
    }

    public void setGameState(String text) {
        gameStateValue.setText(text);
    }

    public void enableUndo(boolean value) {
        undo.setDisable(!value);
    }

    public void enableRedo(boolean value) {
        redo.setDisable(!value);
    }

    public void enableHint(boolean value) {
        hint.setDisable(!value);
    }

    public LogConsole getLogConsole() {
        return logConsole;
    }
}
