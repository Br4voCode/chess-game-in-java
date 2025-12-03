package chess.view;

import chess.controller.GameController;
import chess.game.Game;
import chess.game.HumanPlayer;
import chess.game.AIPlayer;
import chess.model.PieceColor;
import chess.view.components.StatusBar;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class GameView {
    private BorderPane root;
    private ChessBoard chessBoard;
    private StatusBar statusBar;
    private GameController controller;
    
    // Referencias a componentes de UI para actualización
    private Label turnValue;
    private Label gameStateValue;
    private VBox whiteCapturedBox;
    private VBox blackCapturedBox;
    private VBox movesBox;

    public GameView() {
        initializeComponents();
        setupLayout();
    }

    private void initializeComponents() {
        Game game = new Game(new HumanPlayer(), new AIPlayer(PieceColor.BLACK, 3));
        
        this.chessBoard = new ChessBoard();
        this.statusBar = new StatusBar();
        this.controller = new GameController(game, chessBoard, statusBar);
        
        this.chessBoard.setSquareClickListener(controller::onSquareClicked);
    }

    private void setupLayout() {
        root = new BorderPane();
        
        // Panel superior con título
        Label title = new Label("♔ Chess Game ♚");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        HBox titleBar = new HBox(title);
        titleBar.setAlignment(Pos.CENTER);
        titleBar.setPadding(new Insets(10));
        titleBar.setStyle("-fx-background-color: #3c3f41;");
        
        // Paneles laterales
        VBox leftPanel = createLeftPanel();
        VBox rightPanel = createRightPanel();
        
        // Panel central
        VBox centerPanel = createCenterPanel();
        
        // Configurar layout
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
        });
        
        // Información del turno
        Label turnLabel = new Label("Current turn:");
        turnLabel.setStyle("-fx-text-fill: #bbbbbb;");
        
        turnValue = new Label("White");
        turnValue.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        // Estado del juego
        Label gameStateLabel = new Label("Game state:");
        gameStateLabel.setStyle("-fx-text-fill: #bbbbbb;");
        
        gameStateValue = new Label("In progress");
        gameStateValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #4caf50;");
        
        panel.getChildren().addAll(
            panelTitle,
            newGameButton,
            new Pane(), // Espaciador
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
        
        // Piezas capturadas
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
        
        // Historial de movimientos
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
        
        // Tablero con animaciones
        StackPane boardWithAnimations = chessBoard.getBoardWithAnimations();
        
        // Configurar tamaño
        boardWithAnimations.setPrefSize(480, 480);
        boardWithAnimations.setMaxSize(480, 480);
        boardWithAnimations.setMinSize(480, 480);
        
        chessBoard.getBoard().setPrefSize(480, 480);
        chessBoard.getBoard().setMaxSize(480, 480);
        chessBoard.getBoard().setMinSize(480, 480);
        
        // Instrucciones
        Label helpLabel = new Label("Click a piece to select it, then click a destination square");
        helpLabel.setStyle("-fx-text-fill: #bbbbbb; -fx-font-size: 12px;");
        
        VBox boardContainer = new VBox(10, boardWithAnimations, helpLabel);
        boardContainer.setAlignment(Pos.CENTER);
        
        panel.getChildren().add(boardContainer);
        
        return panel;
    }

    public BorderPane getRoot() {
        return root;
    }
    
    // Métodos para actualizar la UI desde el controlador
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
        VBox targetBox = isWhitePiece ? whiteCapturedBox : blackCapturedBox;
        if (targetBox != null) {
            Label pieceLabel = new Label(pieceSymbol);
            pieceLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: " + 
                              (isWhitePiece ? "white" : "#2b2b2b") + ";");
            targetBox.getChildren().add(pieceLabel);
        }
    }
    
    public void addMoveToHistory(String moveNotation) {
        if (movesBox != null) {
            Label moveLabel = new Label(moveNotation);
            moveLabel.setStyle("-fx-text-fill: white;");
            movesBox.getChildren().add(moveLabel);
            
            // Limitar a últimos 10 movimientos
            if (movesBox.getChildren().size() > 10) {
                movesBox.getChildren().remove(0);
            }
        }
    }
}