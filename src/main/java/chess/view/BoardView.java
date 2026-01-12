package chess.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import chess.view.components.*;
import chess.model.pieces.PieceColor;
import chess.model.Position;

public class BoardView {

    private VBox root;
    private ChessBoard chessBoard;
    private TimerBar whiteTimer;
    private TimerBar blackTimer;

    public BoardView() {
        build();
    }

    private void build() {
        root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(0));
        root.setMaxHeight(Double.MAX_VALUE);
        root.setMaxWidth(Double.MAX_VALUE);
        root.setStyle("-fx-background-color: #2b2b2b;");
        VBox.setVgrow(root, Priority.ALWAYS);

        chessBoard = new ChessBoard();
        whiteTimer = new TimerBar(PieceColor.WHITE);
        blackTimer = new TimerBar(PieceColor.BLACK);

        // Center panel con el tablero
        VBox boardContainer = new VBox(10);
        boardContainer.setAlignment(Pos.CENTER);
        boardContainer.setMaxHeight(Double.MAX_VALUE);
        boardContainer.setMaxWidth(Double.MAX_VALUE);
        boardContainer.setPadding(new Insets(10));
        boardContainer.setStyle("-fx-background-color: #2b2b2b;");
        VBox.setVgrow(boardContainer, Priority.ALWAYS);

        // Main board VBox
        VBox mainBoardVBox = new VBox(10);
        mainBoardVBox.setAlignment(Pos.CENTER);
        mainBoardVBox.setMaxHeight(Double.MAX_VALUE);
        mainBoardVBox.setMaxWidth(Double.MAX_VALUE);
        mainBoardVBox.setStyle("-fx-background-color: #2b2b2b;");
        VBox.setVgrow(mainBoardVBox, Priority.ALWAYS);

        // Timer containers
        HBox topTimerContainer = new HBox();
        topTimerContainer.setAlignment(Pos.CENTER);
        if (blackTimer != null) {
            blackTimer.prefWidthProperty().bind(chessBoard.getBoard().widthProperty());
            topTimerContainer.getChildren().add(blackTimer);
        }

        StackPane boardWithAnimations = chessBoard.getBoardWithAnimations();
        boardWithAnimations.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        VBox.setVgrow(boardWithAnimations, Priority.ALWAYS);

        HBox bottomTimerContainer = new HBox();
        bottomTimerContainer.setAlignment(Pos.CENTER);
        if (whiteTimer != null) {
            whiteTimer.prefWidthProperty().bind(chessBoard.getBoard().widthProperty());
            bottomTimerContainer.getChildren().add(whiteTimer);
        }

        mainBoardVBox.getChildren().addAll(topTimerContainer, boardWithAnimations, bottomTimerContainer);

        // Help label
        Label helpLabel = new Label("Click a piece to select it, then click a destination square");
        helpLabel.setStyle("-fx-text-fill: #bbbbbb; -fx-font-size: 12px;");

        boardContainer.getChildren().addAll(mainBoardVBox, helpLabel);

        root.getChildren().add(boardContainer);
    }

    public VBox getRoot() {
        return root;
    }

    // ==== Eventos ====
    public void onSquareClick(java.util.function.Consumer<Position> handler) {
        chessBoard.setSquareClickListener(handler);
    }

    // ==== UI ====
    public void setBottomColor(PieceColor color) {
        chessBoard.setBottomColor(color);
    }

    public ChessBoard getChessBoard() {
        return chessBoard;
    }

    public void updateTimers(long whiteMillis, long blackMillis) {
        whiteTimer.updateTimerDisplay();
        blackTimer.updateTimerDisplay();
    }
}
