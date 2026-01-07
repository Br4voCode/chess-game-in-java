package chess.view;

import chess.model.Piece;
import chess.model.PieceColor;
import chess.model.PieceType;
import chess.model.pieces.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class PromotionDialog {
    private PieceType selectedPiece = PieceType.QUEEN;
    private Stage dialog;
    private boolean confirmed = false;
    private boolean cancelled = false;
    private double xOffset = 0;
    private double yOffset = 0;

    public PieceType showDialog(PieceColor color) {
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setResizable(false);

        VBox root = new VBox();
        root.setStyle("-fx-background-color: #3c3f41; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 15, 0, 0, 5);");

        // Header arrastrable
        HBox header = createDraggableHeader();
        
        // Contenido principal
        VBox content = new VBox(20);
        content.setPadding(new Insets(20, 25, 25, 25));
        content.setAlignment(Pos.CENTER);

        Label title = new Label("♔ Promoción del Peón ♕");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        HBox pieceBox = new HBox(15);
        pieceBox.setAlignment(Pos.CENTER);

        Button queenBtn = createPieceButton(PieceType.QUEEN, color);
        Button rookBtn = createPieceButton(PieceType.ROOK, color);
        Button bishopBtn = createPieceButton(PieceType.BISHOP, color);
        Button knightBtn = createPieceButton(PieceType.KNIGHT, color);

        queenBtn.setStyle(getSelectedButtonStyle());

        queenBtn.setOnAction(e -> selectPiece(PieceType.QUEEN, queenBtn, rookBtn, bishopBtn, knightBtn));
        rookBtn.setOnAction(e -> selectPiece(PieceType.ROOK, rookBtn, queenBtn, bishopBtn, knightBtn));
        bishopBtn.setOnAction(e -> selectPiece(PieceType.BISHOP, bishopBtn, queenBtn, rookBtn, knightBtn));
        knightBtn.setOnAction(e -> selectPiece(PieceType.KNIGHT, knightBtn, queenBtn, rookBtn, bishopBtn));

        pieceBox.getChildren().addAll(queenBtn, rookBtn, bishopBtn, knightBtn);

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button confirmBtn = new Button("✓ Confirmar");
        confirmBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; " +
                           "-fx-font-weight: bold; -fx-padding: 10 20; " +
                           "-fx-font-size: 14px; -fx-cursor: hand;");
        confirmBtn.setOnAction(e -> {
            confirmed = true;
            dialog.close();
        });

        Button cancelBtn = new Button("✗ Cancelar");
        cancelBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; " +
                          "-fx-font-weight: bold; -fx-padding: 10 20; " +
                          "-fx-font-size: 14px; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> {
            cancelled = true;
            dialog.close();
        });

        buttonBox.getChildren().addAll(confirmBtn, cancelBtn);
        content.getChildren().addAll(title, pieceBox, buttonBox);
        
        root.getChildren().addAll(header, content);

        Scene scene = new Scene(root);
        scene.setFill(null);
        dialog.setScene(scene);
        dialog.centerOnScreen();
        
        dialog.setOnCloseRequest(e -> cancelled = true);

        dialog.showAndWait();
        
        if (cancelled) {
            return null;
        }
        return selectedPiece;
    }

    private HBox createDraggableHeader() {
        HBox header = new HBox();
        header.setPrefHeight(30);
        header.setStyle("-fx-background-color: #2b2b2b;");
        header.setPadding(new Insets(5, 10, 5, 10));
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label headerTitle = new Label("Promoción");
        headerTitle.setStyle("-fx-text-fill: #bbbbbb; -fx-font-size: 12px;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button closeBtn = new Button("×");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #bbbbbb; " +
                         "-fx-font-size: 16px; -fx-padding: 0 5; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> {
            cancelled = true;
            dialog.close();
        });
        
        header.getChildren().addAll(headerTitle, spacer, closeBtn);
        
        // Hacer el header arrastrable
        header.setOnMousePressed((MouseEvent event) -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        
        header.setOnMouseDragged((MouseEvent event) -> {
            dialog.setX(event.getScreenX() - xOffset);
            dialog.setY(event.getScreenY() - yOffset);
        });
        
        return header;
    }

    private Button createPieceButton(PieceType pieceType, PieceColor color) {
        Button button = new Button();
        
        VBox content = new VBox(8);
        content.setAlignment(Pos.CENTER);
        
        ImageView imageView = new ImageView();
        imageView.setFitWidth(50);
        imageView.setFitHeight(50);
        imageView.setPreserveRatio(true);
        
        String imagePath = getPieceImagePath(pieceType, color);
        try {
            Image image = new Image(getClass().getResourceAsStream(imagePath));
            imageView.setImage(image);
        } catch (Exception e) {
            Label fallback = new Label(getPieceUnicode(pieceType, color));
            fallback.setStyle("-fx-font-size: 40px; -fx-text-fill: " + 
                             (color == PieceColor.WHITE ? "white" : "#2b2b2b") + ";");
            content.getChildren().add(fallback);
        }
        
        if (imageView.getImage() != null) {
            content.getChildren().add(imageView);
        }
        
        Label nameLabel = new Label(getPieceName(pieceType));
        nameLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: white;");
        content.getChildren().add(nameLabel);
        
        button.setGraphic(content);
        button.setStyle(getDefaultButtonStyle());
        button.setPrefSize(90, 90);
        
        return button;
    }

    private String getPieceImagePath(PieceType type, PieceColor color) {
        String colorPrefix = color == PieceColor.WHITE ? "white" : "black";
        String pieceName = type.name().toLowerCase();
        return "/images/pieces/classic/" + colorPrefix + "_" + pieceName + ".png";
    }

    private String getPieceUnicode(PieceType type, PieceColor color) {
        boolean isWhite = color == PieceColor.WHITE;
        switch (type) {
            case QUEEN: return isWhite ? "♕" : "♛";
            case ROOK: return isWhite ? "♖" : "♜";
            case BISHOP: return isWhite ? "♗" : "♝";
            case KNIGHT: return isWhite ? "♘" : "♞";
            default: return "?";
        }
    }

    private String getPieceName(PieceType type) {
        switch (type) {
            case QUEEN: return "Reina";
            case ROOK: return "Torre";
            case BISHOP: return "Alfil";
            case KNIGHT: return "Caballo";
            default: return "?";
        }
    }

    private void selectPiece(PieceType pieceType, Button selected, Button... others) {
        selectedPiece = pieceType;
        selected.setStyle(getSelectedButtonStyle());
        for (Button other : others) {
            other.setStyle(getDefaultButtonStyle());
        }
    }

    private String getDefaultButtonStyle() {
        return "-fx-background-color: #2b2b2b; " +
               "-fx-border-color: #555555; " +
               "-fx-border-width: 2; " +
               "-fx-cursor: hand;";
    }

    private String getSelectedButtonStyle() {
        return "-fx-background-color: #4caf50; " +
               "-fx-border-color: #66bb6a; " +
               "-fx-border-width: 3; " +
               "-fx-cursor: hand;";
    }

    public boolean wasCancelled() {
        return cancelled;
    }
}