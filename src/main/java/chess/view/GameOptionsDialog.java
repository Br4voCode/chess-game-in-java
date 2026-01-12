package chess.view;

import chess.game.GameSettings;
import chess.model.PieceColor;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Diálogo modal para seleccionar opciones de juego según el modo seleccionado.
 * - Humano vs IA: mostrar color y dificultad
 * - Humano vs Humano: sin opciones
 * - IA vs IA: solo dificultad
 */
public class GameOptionsDialog {
    private final StackPane container;
    private PieceColor selectedColor = PieceColor.WHITE;
    private int selectedDifficulty = GameSettings.DEFAULT_DEPTH;
    private boolean confirmed = false;
    private Runnable onConfirm;
    private Runnable onCancel;

    public GameOptionsDialog(String gameMode) {
        this.container = new StackPane();
        initDialog(gameMode);
    }

    private void initDialog(String gameMode) {
        
        VBox backdrop = new VBox();
        backdrop.setStyle("-fx-background-color: rgba(0,0,0,0.5);");
        backdrop.setPrefHeight(Double.MAX_VALUE);
        backdrop.setPrefWidth(Double.MAX_VALUE);

        
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(30));
        mainContent.setStyle(
                "-fx-background-color: rgba(33,33,35,0.98);" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: rgba(255,255,255,0.1);" +
                        "-fx-border-radius: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 40, 0, 0, 20);"
        );
        mainContent.setMaxWidth(450);
        mainContent.setPrefWidth(450);
        mainContent.setMaxHeight(480);
        mainContent.setAlignment(Pos.TOP_CENTER);

        Label titleLabel = new Label("Configura tu partida");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");

        VBox contentBox = new VBox(20);
        contentBox.setAlignment(Pos.TOP_CENTER);

        if ("humanVsAI".equals(gameMode)) {
            contentBox.getChildren().addAll(
                    createColorSelector(),
                    createDifficultySelector()
            );
        } else if ("aiVsAI".equals(gameMode)) {
            contentBox.getChildren().add(createDifficultySelector());
        } else if ("humanVsHuman".equals(gameMode)) {
            Label noOptionsLabel = new Label("No hay opciones para configurar.\n¡Que disfrutes la partida!");
            noOptionsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #bdbdbd; -fx-text-alignment: center; -fx-wrap-text: true;");
            contentBox.getChildren().add(noOptionsLabel);
        }

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        Button confirmButton = new Button("Comenzar Partida");
        confirmButton.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-padding: 12px 40px; " +
                        "-fx-background-color: #4caf50; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand;"
        );
        confirmButton.setOnAction(e -> {
            confirmed = true;
            hideDialog();
            if (onConfirm != null) onConfirm.run();
        });

        Button cancelButton = new Button("Cancelar");
        cancelButton.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-padding: 12px 40px; " +
                        "-fx-background-color: #f44336; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand;"
        );
        cancelButton.setOnAction(e -> {
            confirmed = false;
            hideDialog();
            if (onCancel != null) onCancel.run();
        });

        buttonBox.getChildren().addAll(confirmButton, cancelButton);

        mainContent.getChildren().addAll(titleLabel, contentBox, buttonBox);

        
        StackPane.setAlignment(mainContent, Pos.CENTER);
        container.getChildren().addAll(backdrop, mainContent);
        container.setPrefHeight(Double.MAX_VALUE);
        container.setPrefWidth(Double.MAX_VALUE);
        container.setVisible(false);
    }

    private VBox createColorSelector() {
        VBox selectorBox = new VBox(12);
        selectorBox.setPadding(new Insets(15));
        selectorBox.setStyle(
                "-fx-background-color: rgba(255,255,255,0.04);" +
                        "-fx-border-color: rgba(255,255,255,0.12);" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;"
        );

        Label colorLabel = new Label("Color de piezas");
        colorLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        ToggleGroup colorGroup = new ToggleGroup();
        ToggleButton whiteToggle = createColorToggle("Jugar con blancas", PieceColor.WHITE, colorGroup);
        ToggleButton blackToggle = createColorToggle("Jugar con negras", PieceColor.BLACK, colorGroup);

        whiteToggle.setSelected(true);

        HBox colorBox = new HBox(10);
        colorBox.setAlignment(Pos.CENTER);
        colorBox.getChildren().addAll(whiteToggle, blackToggle);

        selectorBox.getChildren().addAll(colorLabel, colorBox);
        return selectorBox;
    }

    private VBox createDifficultySelector() {
        VBox selectorBox = new VBox(12);
        selectorBox.setPadding(new Insets(15));
        selectorBox.setStyle(
                "-fx-background-color: rgba(255,255,255,0.04);" +
                        "-fx-border-color: rgba(255,255,255,0.12);" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;"
        );

        Label difficultyLabel = new Label("Dificultad del AI");
        difficultyLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label difficultyValueLabel = new Label(formatDifficultyLabel(selectedDifficulty));
        difficultyValueLabel.setStyle("-fx-text-fill: #4caf50; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label warningLabel = new Label("⚠ ALERTA: Su hardware no es suficiente para este nivel de profundidad");
        warningLabel.setStyle(
                "-fx-text-fill: #ff9800; " +
                        "-fx-font-size: 11px; " +
                        "-fx-wrap-text: true; " +
                        "-fx-padding: 12; " +
                        "-fx-background-color: rgba(255, 152, 0, 0.15); " +
                        "-fx-background-radius: 8; " +
                        "-fx-border-color: rgba(255, 152, 0, 0.4); " +
                        "-fx-border-radius: 8; " +
                        "-fx-line-spacing: 1.2;"
        );
        warningLabel.setVisible(selectedDifficulty == 4);

        Slider difficultySlider = new Slider(GameSettings.MIN_DEPTH, GameSettings.MAX_DEPTH, selectedDifficulty);
        difficultySlider.setMajorTickUnit(1);
        difficultySlider.setMinorTickCount(0);
        difficultySlider.setShowTickMarks(true);
        difficultySlider.setShowTickLabels(true);
        difficultySlider.setSnapToTicks(true);
        difficultySlider.setPrefWidth(300);
        difficultySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            selectedDifficulty = newVal.intValue();
            difficultyValueLabel.setText(formatDifficultyLabel(selectedDifficulty));
            warningLabel.setVisible(selectedDifficulty == 4);
        });

        Label difficultyHint = new Label("1 = rápido • 2 = fácil • 3 = normal • 4 = maestro");
        difficultyHint.setStyle("-fx-text-fill: #8f8f8f; -fx-font-size: 12px;");

        selectorBox.getChildren().addAll(
                difficultyLabel,
                difficultyValueLabel,
                difficultySlider,
                difficultyHint,
                warningLabel
        );
        return selectorBox;
    }

    private ToggleButton createColorToggle(String text, PieceColor color, ToggleGroup group) {
        ToggleButton toggle = new ToggleButton(text);
        toggle.setToggleGroup(group);
        toggle.setPrefWidth(140);

        final String baseStyle = "-fx-font-size: 13px;" +
                "-fx-background-color: rgba(255,255,255,0.08);" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 10;" +
                "-fx-padding: 10 15;";
        final String selectedStyle = "-fx-font-size: 13px;" +
                "-fx-background-color: linear-gradient(to right, #4caf50, #2e7d32);" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 10;" +
                "-fx-padding: 10 15;" +
                "-fx-effect: dropshadow(gaussian, rgba(76,175,80,0.35), 15, 0, 0, 4);";

        toggle.setStyle(baseStyle);

        toggle.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                selectedColor = color;
                toggle.setStyle(selectedStyle);
            } else {
                toggle.setStyle(baseStyle);
            }
        });

        return toggle;
    }

    private String formatDifficultyLabel(int depth) {
        return GameSettings.describeDepth(depth);
    }

    public StackPane getContainer() {
        return container;
    }

    public void showDialog(Runnable onConfirmCallback, Runnable onCancelCallback) {
        this.onConfirm = onConfirmCallback;
        this.onCancel = onCancelCallback;
        confirmed = false;
        container.setVisible(true);
    }

    private void hideDialog() {
        container.setVisible(false);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public PieceColor getSelectedColor() {
        return selectedColor;
    }

    public int getSelectedDifficulty() {
        return selectedDifficulty;
    }
}
