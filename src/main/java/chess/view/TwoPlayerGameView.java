package chess.view;

import chess.controller.GameController;
import chess.game.Game;
import chess.game.HumanPlayer;
import chess.model.PieceColor;
import chess.view.components.StatusBar;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class TwoPlayerGameView implements IGameView {
    private BorderPane root;
    private ChessBoard chessBoard;
    private StatusBar statusBar;
    private GameController controller;
    
    private Label turnValue;
    private Label gameStateValue;
    private VBox whiteCapturedBox;
    private VBox blackCapturedBox;
    private VBox movesBox;
    
    private Game gameInstance;
    private Runnable onBackToMenu;

    public TwoPlayerGameView(Runnable onBackToMenu) {
        this.onBackToMenu = onBackToMenu;
        initializeComponents();
        setupLayout();
    }

    private void initializeComponents() {
        Game game = new Game(new HumanPlayer(), new HumanPlayer());
        
        this.gameInstance = game;
        this.chessBoard = new ChessBoard();
        this.statusBar = new StatusBar();
        this.controller = new GameController(game, chessBoard, statusBar);
        this.controller.setGameView(this);
        
        this.chessBoard.setSquareClickListener(controller::onSquareClicked);
    }

    private void setupLayout() {
        root = new BorderPane();
        
        Label title = new Label("♔ Chess Game - Two Player Mode ♚");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        HBox titleBar = new HBox(title);
        titleBar.setAlignment(Pos.CENTER);
        titleBar.setPadding(new Insets(10));
        titleBar.setStyle("-fx-background-color: #3c3f41;");
        
        VBox leftPanel = createLeftPanel();
        VBox rightPanel = createRightPanel();
        VBox centerPanel = createCenterPanel();
        
        root.setTop(titleBar);
        root.setLeft(leftPanel);
        root.setCenter(centerPanel);
        root.setRight(rightPanel);
        root.setBottom(statusBar.getRoot());
        
        root.setStyle("-fx-background-color: #2b2b2b;");
    }

    private VBox createLeftPanel() {
        VBox panel = new VBox(10);
        panel.setPrefWidth(180);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #3c3f41; -fx-background-radius: 5;");
        
        Label panelTitle = new Label("Game Controls");
        panelTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Button newGameButton = new Button("New Game");
        newGameButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold;");
        newGameButton.setMaxWidth(Double.MAX_VALUE);
        newGameButton.setOnAction(e -> {
            controller.resetGame();
            updateUIFromController();
            clearCapturedPieces();
            clearMoveHistory();
        });
        
        Button backButton = new Button("Back to Menu");
        backButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
        backButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setOnAction(e -> {
            if (onBackToMenu != null) {
                onBackToMenu.run();
            }
        });
        
        Label turnLabel = new Label("Current turn:");
        turnLabel.setStyle("-fx-text-fill: #bbbbbb;");
        
        turnValue = new Label("White");
        turnValue.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label gameStateLabel = new Label("Game state:");
        gameStateLabel.setStyle("-fx-text-fill: #bbbbbb;");
        
        gameStateValue = new Label("In progress");
        gameStateValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #4caf50;");
        
        panel.getChildren().addAll(
            panelTitle,
            newGameButton,
            backButton,
            new Pane(),
            turnLabel, turnValue,
            gameStateLabel, gameStateValue
        );
        
        return panel;
    }

    private VBox createRightPanel() {
        VBox panel = new VBox(10);
        panel.setPrefWidth(200);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #3c3f41; -fx-background-radius: 5;");
        
        Label panelTitle = new Label("Game Info");
        panelTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label whiteCapturedTitle = new Label("White captured:");
        whiteCapturedTitle.setStyle("-fx-text-fill: #bbbbbb;");
        
        whiteCapturedBox = new VBox(5);
        whiteCapturedBox.setStyle("-fx-background-color: #2b2b2b; -fx-padding: 10; -fx-background-radius: 3;");
        whiteCapturedBox.setPrefHeight(100);
        
        Label blackCapturedTitle = new Label("Black captured:");
        blackCapturedTitle.setStyle("-fx-text-fill: #bbbbbb;");
        
        blackCapturedBox = new VBox(5);
        blackCapturedBox.setStyle("-fx-background-color: #2b2b2b; -fx-padding: 10; -fx-background-radius: 3;");
        blackCapturedBox.setPrefHeight(100);
        
        Label movesTitle = new Label("Move history:");
        movesTitle.setStyle("-fx-text-fill: #bbbbbb;");
        
        movesBox = new VBox(3);
        movesBox.setStyle("-fx-background-color: #2b2b2b; -fx-padding: 10; -fx-background-radius: 3;");
        movesBox.setPrefHeight(150);
        
        panel.getChildren().addAll(
            panelTitle,
            whiteCapturedTitle, whiteCapturedBox,
            blackCapturedTitle, blackCapturedBox,
            movesTitle, movesBox
        );
        
        return panel;
    }

    private VBox createCenterPanel() {
        VBox panel = new VBox();
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(20));
        
        StackPane boardWithAnimations = chessBoard.getBoardWithAnimations();
        
        boardWithAnimations.setPrefSize(480, 480);
        boardWithAnimations.setMaxSize(480, 480);
        boardWithAnimations.setMinSize(480, 480);
        
        chessBoard.getBoard().setPrefSize(480, 480);
        chessBoard.getBoard().setMaxSize(480, 480);
        chessBoard.getBoard().setMinSize(480, 480);
        
        Label helpLabel = new Label("Two Player Mode: White and Black take turns");
        helpLabel.setStyle("-fx-text-fill: #bbbbbb; -fx-font-size: 12px;");
        
        VBox boardContainer = new VBox(10, boardWithAnimations, helpLabel);
        boardContainer.setAlignment(Pos.CENTER);
        
        panel.getChildren().add(boardContainer);
        
        return panel;
    }

    public BorderPane getRoot() {
        return root;
    }
    
    public void updateUIFromController() {
        updateTurnDisplay(controller.getCurrentTurn());
        updateGameState("In progress");
    }
    
    private void updateTurnDisplay(PieceColor currentTurn) {
        if (turnValue != null) {
            turnValue.setText(currentTurn == PieceColor.WHITE ? "White" : "Black");
        }
    }
    
    private void updateGameState(String state) {
        if (gameStateValue != null) {
            gameStateValue.setText(state);
        }
    }
    
    public void addCapturedPiece(String pieceSymbol, boolean isWhitePiece) {
        // White captured box shows pieces that white captured (black pieces)
        // Black captured box shows pieces that black captured (white pieces)
        VBox targetBox = isWhitePiece ? blackCapturedBox : whiteCapturedBox;
        if (targetBox != null) {
            // Invert the piece symbol to show the color of the capturing player
            String displaySymbol = invertPieceSymbol(pieceSymbol, isWhitePiece);
            Label pieceLabel = new Label(displaySymbol);
            // Use same white text color as move history for consistent appearance
            String textColor = "white";
            pieceLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: " + textColor + ";");
            targetBox.getChildren().add(pieceLabel);
        }
    }
    
    private String invertPieceSymbol(String pieceSymbol, boolean isWhitePiece) {
        // If the captured piece is white, show black symbol (captured by black player)
        // If the captured piece is black, show white symbol (captured by white player)
        switch (pieceSymbol) {
            // White pieces -> show as black symbols
            case "♔": return "♚";
            case "♕": return "♛";
            case "♖": return "♜";
            case "♗": return "♝";
            case "♘": return "♞";
            case "♙": return "♟";
            
            // Black pieces -> show as white symbols
            case "♚": return "♔";
            case "♛": return "♕";
            case "♜": return "♖";
            case "♝": return "♗";
            case "♞": return "♘";
            case "♟": return "♙";
            
            default: return pieceSymbol; // Fallback
        }
    }
    
    public void clearCapturedPieces() {
        if (whiteCapturedBox != null) {
            whiteCapturedBox.getChildren().clear();
        }
        if (blackCapturedBox != null) {
            blackCapturedBox.getChildren().clear();
        }
    }
    
    public void addMoveToHistoryWithColor(String moveNotation, chess.model.PieceColor color) {
        if (movesBox != null) {
            String icon = (color == chess.model.PieceColor.WHITE) ? "♟" : "♙";
            
            Label moveLabel = new Label(icon + " " + moveNotation);
            moveLabel.setStyle("-fx-text-fill: white;");
            movesBox.getChildren().add(moveLabel);
            
            if (movesBox.getChildren().size() > 10) {
                movesBox.getChildren().remove(0);
            }
        }
    }
    
    public void clearMoveHistory() {
        if (movesBox != null) {
            movesBox.getChildren().clear();
        }
    }
}
