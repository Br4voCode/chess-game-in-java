package chess.view;

import chess.model.pieces.PieceColor;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class RightPanelView {

    private VBox root;
    private VBox movesBox;
    private FlowPane whiteCaptured;
    private FlowPane blackCaptured;

    public RightPanelView() {
        build();
    }

    private void build() {
        root = new VBox(10);
        root.setPrefWidth(200);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #3c3f41; -fx-background-radius: 5;");
        root.setMaxHeight(Double.MAX_VALUE);

        Label panelTitle = new Label("Game Info");
        panelTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label whiteCapturedTitle = new Label("White captures:");
        whiteCapturedTitle.setStyle("-fx-text-fill: #bbbbbb;");

        whiteCaptured = new FlowPane(5, 5);
        whiteCaptured.setStyle("-fx-background-color: #2b2b2b; -fx-padding: 10; -fx-background-radius: 3;");
        whiteCaptured.setPrefHeight(100);
        whiteCaptured.setMaxWidth(Double.MAX_VALUE);

        Label blackCapturedTitle = new Label("Black captures:");
        blackCapturedTitle.setStyle("-fx-text-fill: #bbbbbb;");

        blackCaptured = new FlowPane(5, 5);
        blackCaptured.setStyle("-fx-background-color: #2b2b2b; -fx-padding: 10; -fx-background-radius: 3;");
        blackCaptured.setPrefHeight(100);
        blackCaptured.setMaxWidth(Double.MAX_VALUE);

        Label movesTitle = new Label("Move history:");
        movesTitle.setStyle("-fx-text-fill: #bbbbbb;");

        movesBox = new VBox(3);
        movesBox.setStyle("-fx-background-color: #2b2b2b; -fx-padding: 10; -fx-background-radius: 3;");
        movesBox.setPrefHeight(100);
        VBox.setVgrow(movesBox, Priority.ALWAYS);

        root.getChildren().addAll(
                panelTitle,
                whiteCapturedTitle, whiteCaptured,
                blackCapturedTitle, blackCaptured,
                movesTitle, movesBox);
    }

    public VBox getRoot() {
        return root;
    }

    // ==== Moves ====
    public void addMove(String text, PieceColor color) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: white;");
        movesBox.getChildren().add(l);
    }

    public void removeLastMove() {
        if (!movesBox.getChildren().isEmpty()) {
            movesBox.getChildren().remove(movesBox.getChildren().size() - 1);
        }
    }

    // ==== Captures ====
    public void addCaptured(String piece, PieceColor color) {
        Label l = new Label(piece);
        l.setStyle("-fx-font-size: 20px; -fx-text-fill: " +
                (color == PieceColor.WHITE ? "black" : "white") + ";");
        if (color == PieceColor.WHITE) {
            blackCaptured.getChildren().add(l);
        } else {
            whiteCaptured.getChildren().add(l);
        }
    }

    public void removeLastCaptured(PieceColor color) {
        FlowPane box = (color == PieceColor.WHITE) ? blackCaptured : whiteCaptured;
        if (!box.getChildren().isEmpty()) {
            box.getChildren().remove(box.getChildren().size() - 1);
        }
    }

    // ==== Reset/Clear ====
    public void clearMovesHistory() {
        movesBox.getChildren().clear();
    }

    public void clearCapturedPieces() {
        whiteCaptured.getChildren().clear();
        blackCaptured.getChildren().clear();
    }
}
