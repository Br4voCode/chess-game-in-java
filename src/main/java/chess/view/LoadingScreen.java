package chess.view;

import chess.game.GameSettings;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * Transitional loading view that visualizes the boot process between the home screen and the game board.
 */
public class LoadingScreen {
    private final StackPane root;
    private final Label statusLabel;
    private final Label percentageLabel;
    private final Label depthLabel;
    private final ProgressBar progressBar;
    private final Button cancelButton;
    private Task<?> boundTask;

    public LoadingScreen(String headline, GameSettings settings, Runnable onCancel) {
        root = new StackPane();
        root.setPadding(new Insets(60));
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #050505, #121317, #0d1b2a);");

        Circle neonSoft = new Circle(260, Color.web("#4caf5040"));
        neonSoft.setTranslateX(-220);
        neonSoft.setTranslateY(-180);
        neonSoft.setEffect(new GaussianBlur(120));
        neonSoft.setMouseTransparent(true);

        Circle neonAccent = new Circle(200, Color.web("#2196f355"));
        neonAccent.setTranslateX(240);
        neonAccent.setTranslateY(160);
        neonAccent.setEffect(new GaussianBlur(90));
        neonAccent.setMouseTransparent(true);

        VBox card = new VBox(24);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40, 70, 40, 70));
        card.setMaxWidth(520);
        card.setStyle(
                "-fx-background-color: rgba(10, 12, 18, 0.9);" +
                        "-fx-background-radius: 32;" +
                        "-fx-border-radius: 34;" +
                        "-fx-border-color: rgba(255,255,255,0.08);" +
                        "-fx-border-width: 1.4px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.65), 45, 0, 0, 25);");

        Label headlineLabel = new Label(headline != null ? headline : "Preparando partida");
        headlineLabel.setStyle(
                "-fx-font-size: 28px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-family: 'Poppins','Helvetica Neue',sans-serif;");

        depthLabel = new Label(buildDepthLabel(settings));
        depthLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.75); -fx-font-size: 14px;");

        statusLabel = new Label("Iniciando...");
        statusLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.78); -fx-font-size: 15px;");

        progressBar = new ProgressBar(0);
        progressBar.setPrefHeight(10);
        progressBar.setPrefWidth(360);
        progressBar.setStyle(
                "-fx-accent: linear-gradient(to right, #4caf50, #00c6ff);" +
                        "-fx-background-insets: 0;" +
                        "-fx-background-radius: 999;" +
                        "-fx-padding: 2;" +
                        "-fx-control-inner-background: rgba(255,255,255,0.08);");

        percentageLabel = new Label("0%");
        percentageLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.65); -fx-font-size: 14px;");

        HBox progressRow = new HBox(12, progressBar, percentageLabel);
        progressRow.setAlignment(Pos.CENTER_LEFT);

        Label tipLabel = new Label(buildTip(settings));
        tipLabel.setWrapText(true);
        tipLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.6); -fx-font-size: 13px;");

        cancelButton = new Button("Cancelar y volver");
        cancelButton.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: rgba(255,255,255,0.35);" +
                        "-fx-border-radius: 999;" +
                        "-fx-text-fill: rgba(255,255,255,0.85);" +
                        "-fx-padding: 10 28;" +
                        "-fx-font-size: 13px;" +
                        "-fx-cursor: hand;");
        cancelButton.setOnAction(e -> {
            if (boundTask != null) {
                boundTask.cancel(true);
            }
            if (onCancel != null) {
                onCancel.run();
            }
        });

        card.getChildren().addAll(headlineLabel, depthLabel, statusLabel, progressRow, tipLabel, cancelButton);

        root.getChildren().addAll(neonSoft, neonAccent, card);
    }

    private String buildDepthLabel(GameSettings settings) {
        if (settings == null) {
            return "Analizando configuraciones";
        }
        if (settings.isPlayerVsPlayer()) {
            return "Modo: 2 jugadores · Sin IA activa";
        }
        if (settings.isAIVsAI()) {
            return "Modo: IA vs IA · " + GameSettings.describeDepth(settings.getAiDepth());
        }
        return "Modo: Humano vs IA · " + GameSettings.describeDepth(settings.getAiDepth());
    }

    private String buildTip(GameSettings settings) {
        if (settings == null) {
            return "Optimizando heurísticas iniciales";
        }
        if (settings.isPlayerVsPlayer()) {
            return "Sin IA: sincronizando historial y preparando tablero compartido.";
        }
        return "Profundidad " + settings.getAiDepth()
                + ": cargando evaluaciones para anticipar hasta " + settings.getAiDepth() * 2 + " jugadas.";
    }

    public StackPane getRoot() {
        return root;
    }

    public void bindToTask(Task<?> task) {
        this.boundTask = task;
        progressBar.progressProperty().bind(task.progressProperty());
        statusLabel.textProperty().bind(task.messageProperty());
        percentageLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            double progress = task.getProgress();
            if (progress < 0) {
                return "0%";
            }
            int pct = (int) Math.min(100, Math.round(progress * 100));
            return pct + "%";
        }, task.progressProperty()));
    }

    public void showError(String message, Runnable onReturn) {
        if (boundTask != null) {
            progressBar.progressProperty().unbind();
        }
        statusLabel.textProperty().unbind();
        percentageLabel.textProperty().unbind();
        progressBar.setProgress(0);
        percentageLabel.setText("0%");
        statusLabel.setText(message != null ? message : "No se pudo inicializar la partida");
        statusLabel.setStyle("-fx-text-fill: #ff8a80; -fx-font-size: 14px;");
        cancelButton.setText("Volver al inicio");
        cancelButton.setDisable(false);
        cancelButton.setOnAction(e -> {
            if (onReturn != null) {
                onReturn.run();
            }
        });
    }
}
