package chess.view;

import chess.controller.GameController;
import chess.game.AIMatch;
import chess.game.AIPlayer;
import chess.game.Game;
import chess.game.GameReconstructor;
import chess.game.HumanPlayer;
import chess.model.PieceColor;
import chess.view.components.StatusBar;
import chess.view.components.TimerBar;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class GameView {
    private BorderPane root;
    private ChessBoard chessBoard;
    private StatusBar statusBar;
    private GameController controller;

    // Referencias a componentes de UI para actualización
    private Label turnValue;
    private Label gameStateValue;
    private FlowPane whiteCapturedBox;
    private FlowPane blackCapturedBox;
    private VBox movesBox;
     // Timer components
    private TimerBar blackTimerBar;
    private TimerBar whiteTimerBar;
    private Game gameInstance;
    private boolean shouldLoadHistory;
    private AIMatch aiMatch;

    public GameView() {
        this(false, false, false);
    }

    public GameView(boolean loadFromHistory) {
        this(loadFromHistory, false, false);
    }

    public GameView(boolean loadFromHistory, boolean isPlayerVsPlayer) {
        this(loadFromHistory, isPlayerVsPlayer, false);
    }

    public GameView(boolean loadFromHistory, boolean isPlayerVsPlayer, boolean isAIVsAI) {
        this.shouldLoadHistory = loadFromHistory;
        initializeComponents(loadFromHistory, isPlayerVsPlayer, isAIVsAI);
        setupLayout();
        updateUIFromController(); // Forzar actualización inicial tras montar el layout

        // Cargar el historial visual después de que setupLayout haya inicializado
        // movesBox
        if (shouldLoadHistory && gameInstance != null && StartScreen.hasGameHistory()) {
            loadMoveHistoryToUI(gameInstance.getMoveHistory());
        }

        // Iniciar partida IA vs IA si es necesario
        if (isAIVsAI) {
            startAIVsAIMatch();
        }
    }

    private void initializeComponents(boolean loadFromHistory, boolean isPlayerVsPlayer, boolean isAIVsAI) {
        Game game;

        if (loadFromHistory && StartScreen.hasGameHistory()) {
            // Cargar partida anterior
            game = GameReconstructor.reconstructGameFromHistory(
                    StartScreen.getHistoryFilePath(),
                    new HumanPlayer(),
                    new AIPlayer(PieceColor.BLACK, 3));
        } else {
            // Crear nueva partida
            if (isAIVsAI) {
                // IA vs IA
                game = new Game(new AIPlayer(PieceColor.WHITE, 3), new AIPlayer(PieceColor.BLACK, 3));
            } else if (isPlayerVsPlayer) {
                // Humano vs Humano
                game = new Game(new HumanPlayer(), new HumanPlayer());
            } else {
                // Humano vs IA
                game = new Game(new HumanPlayer(), new AIPlayer(PieceColor.BLACK, 3));
            }
        }

        this.gameInstance = game;
        this.chessBoard = new ChessBoard();
        this.statusBar = new StatusBar();
        this.controller = new GameController(game, chessBoard, statusBar, isPlayerVsPlayer, isAIVsAI);
        this.controller.setGameView(this);

        this.chessBoard.setSquareClickListener(controller::onSquareClicked);

        // Imprimir el historial al iniciar el juego

    }

    private void setupLayout() {
        root = new BorderPane();
           // Timer bars (se ubican alrededor del tablero)
        blackTimerBar = new TimerBar(gameInstance.getGameClock(), PieceColor.BLACK, true);
        whiteTimerBar = new TimerBar(gameInstance.getGameClock(), PieceColor.WHITE, false);

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
        panel.setMaxHeight(Double.MAX_VALUE);
        
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

        turnValue = new Label(gameInstance.getTurn() == PieceColor.WHITE ? "White" : "Black");
        turnValue.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Estado del juego
        Label gameStateLabel = new Label("Game state:");
        gameStateLabel.setStyle("-fx-text-fill: #bbbbbb;");

        gameStateValue = new Label("In progress");
        gameStateValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #4caf50;");
        Pane spacer = new Pane();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        panel.getChildren().addAll(
          panelTitle,
                newGameButton,
                new Pane(), // Espaciador
                turnLabel, turnValue,
            spacer,
                gameStateLabel, gameStateValue
        );
        
        return panel;
    }

    private VBox createRightPanel() {
        VBox panel = new VBox(10);
        panel.setPrefWidth(200);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #3c3f41; -fx-background-radius: 5;");
        panel.setMaxHeight(Double.MAX_VALUE);
        
        Label panelTitle = new Label("Game Info");
        panelTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Piezas capturadas
        Label whiteCapturedTitle = new Label("White captures:");
        whiteCapturedTitle.setStyle("-fx-text-fill: #bbbbbb;");

        whiteCapturedBox = new FlowPane(5, 5);
        whiteCapturedBox.setStyle("-fx-background-color: #2b2b2b; -fx-padding: 10; -fx-background-radius: 3;");
        whiteCapturedBox.setPrefHeight(100);
        whiteCapturedBox.setMaxWidth(Double.MAX_VALUE);
        
        Label blackCapturedTitle = new Label("Black captures:");
        blackCapturedTitle.setStyle("-fx-text-fill: #bbbbbb;");

        blackCapturedBox = new FlowPane(5, 5);
        blackCapturedBox.setStyle("-fx-background-color: #2b2b2b; -fx-padding: 10; -fx-background-radius: 3;");
        blackCapturedBox.setPrefHeight(100);
        blackCapturedBox.setMaxWidth(Double.MAX_VALUE);
        
        // Historial de movimientos
        Label movesTitle = new Label("Move history:");
        movesTitle.setStyle("-fx-text-fill: #bbbbbb;");

        movesBox = new VBox(3);
        movesBox.setStyle("-fx-background-color: #2b2b2b; -fx-padding: 10; -fx-background-radius: 3;");
        movesBox.setPrefHeight(150);
        VBox.setVgrow(movesBox, Priority.ALWAYS);
        
        panel.getChildren().addAll(
                panelTitle,
                whiteCapturedTitle, whiteCapturedBox,
                blackCapturedTitle, blackCapturedBox,
                movesTitle, movesBox);

        return panel;
    }

    private VBox createCenterPanel() {
        VBox panel = new VBox();
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(0));
        panel.setMaxHeight(Double.MAX_VALUE);
        panel.setMaxWidth(Double.MAX_VALUE);
        panel.setStyle("-fx-background-color: #2b2b2b;");
        VBox.setVgrow(panel, Priority.ALWAYS);
        
        // Tablero con animaciones
        StackPane boardWithAnimations = chessBoard.getBoardWithAnimations();

        // Allow the board to use all available space (it will keep square shape internally)
        boardWithAnimations.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        
        // Instrucciones
        Label helpLabel = new Label("Click a piece to select it, then click a destination square");
        helpLabel.setStyle("-fx-text-fill: #bbbbbb; -fx-font-size: 12px;");

        VBox boardContainer = new VBox(10);
        boardContainer.setAlignment(Pos.CENTER);
        boardContainer.setMaxHeight(Double.MAX_VALUE);
        boardContainer.setMaxWidth(Double.MAX_VALUE);
        boardContainer.setPadding(new Insets(10));
        boardContainer.setStyle("-fx-background-color: #2b2b2b;");
        VBox.setVgrow(boardContainer, Priority.ALWAYS);

        // Contenedor vertical para tablero y relojes (relojes centrados, tablero grande)
        VBox mainBoardVBox = new VBox(10);
        mainBoardVBox.setAlignment(Pos.CENTER);
        mainBoardVBox.setMaxHeight(Double.MAX_VALUE);
        mainBoardVBox.setMaxWidth(Double.MAX_VALUE);
        mainBoardVBox.setStyle("-fx-background-color: #2b2b2b;");
        VBox.setVgrow(mainBoardVBox, Priority.ALWAYS);

        // Reloj superior en un HBox centrado (ancho limitado al tablero)
        HBox topTimerContainer = new HBox();
        topTimerContainer.setAlignment(Pos.CENTER);
        if (blackTimerBar != null) {
            blackTimerBar.prefWidthProperty().bind(chessBoard.getBoard().widthProperty());
            topTimerContainer.getChildren().add(blackTimerBar);
        }

        // El tablero crece libremente
        VBox.setVgrow(boardWithAnimations, Priority.ALWAYS);

        // Reloj inferior en un HBox centrado (ancho limitado al tablero)
        HBox bottomTimerContainer = new HBox();
        bottomTimerContainer.setAlignment(Pos.CENTER);
        if (whiteTimerBar != null) {
            whiteTimerBar.prefWidthProperty().bind(chessBoard.getBoard().widthProperty());
            bottomTimerContainer.getChildren().add(whiteTimerBar);
        }

        mainBoardVBox.getChildren().addAll(topTimerContainer, boardWithAnimations, bottomTimerContainer);
        boardContainer.getChildren().addAll(mainBoardVBox, helpLabel);
        
        panel.getChildren().add(boardContainer);

        return panel;
    }

    public BorderPane getRoot() {
        return root;
    }
    
    /**
     * Update timer displays
     */
    public void updateTimers() {
        if (blackTimerBar != null) {
            blackTimerBar.updateTimerDisplay();
        }
        if (whiteTimerBar != null) {
            whiteTimerBar.updateTimerDisplay();
        }
    }
    
    /**
     * Get the white timer bar
     */
    public TimerBar getWhiteTimerBar() {
        return whiteTimerBar;
    }
    
    /**
     * Get the black timer bar
     */
    public TimerBar getBlackTimerBar() {
        return blackTimerBar;
    }
    
    // Métodos para actualizar la UI desde el controlador
    public void updateUIFromController() {
        if (gameInstance != null) {
            PieceColor currentTurn = gameInstance.getTurn();
            updateTurnDisplay(currentTurn);
            updateGameState(gameInstance.getGameStatus());
        }
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
        // White pieces are captured by black, so they go in black's captured box
        // Black pieces are captured by white, so they go in white's captured box
        FlowPane targetBox = isWhitePiece ? blackCapturedBox : whiteCapturedBox;
        if (targetBox != null) {
            Label pieceLabel = new Label(pieceSymbol);
            pieceLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: " +
                    (isWhitePiece ? "white" : "black") + ";");
            targetBox.getChildren().add(pieceLabel);
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

    public void addMoveToHistoryWithColor(String moveNotation, chess.model.PieceColor color) {
        if (movesBox != null) {
            String icon = (color == chess.model.PieceColor.WHITE) ? "♟" : "♙";

            Label moveLabel = new Label(icon + " " + moveNotation);
            moveLabel.setStyle("-fx-text-fill: white;");
            movesBox.getChildren().add(moveLabel);

            // Limitar a últimos 10 movimientos
            if (movesBox.getChildren().size() > 10) {
                movesBox.getChildren().remove(0);
            }
        }
    }

    private void loadMoveHistoryToUI(chess.model.MoveHistory history) {
        java.util.List<chess.model.Move> moves = history.getMoves();

        for (int i = 0; i < moves.size(); i++) {
            chess.model.Move move = moves.get(i);
            chess.model.Position from = move.getFrom();
            chess.model.Position to = move.getTo();

            char fromFile = (char) ('a' + from.getCol());
            int fromRank = 8 - from.getRow();
            char toFile = (char) ('a' + to.getCol());
            int toRank = 8 - to.getRow();

            String moveNotation = fromFile + "" + fromRank + "-" + toFile + "" + toRank;

            // Determinar el color: índices pares son blancos, impares son negros
            chess.model.PieceColor color = (i % 2 == 0) ? chess.model.PieceColor.WHITE : chess.model.PieceColor.BLACK;

            addMoveToHistoryWithColor(moveNotation, color);
        }
    }

    /**
     * بدء مباراة آلية بين ذكاءين اصطناعيين
     */
    private void startAIVsAIMatch() {
        if (gameInstance == null || !(gameInstance.getWhitePlayer() instanceof AIPlayer) ||
                !(gameInstance.getBlackPlayer() instanceof AIPlayer)) {
            return;
        }

        AIPlayer whiteAI = (AIPlayer) gameInstance.getWhitePlayer();
        AIPlayer blackAI = (AIPlayer) gameInstance.getBlackPlayer();

        aiMatch = new AIMatch(gameInstance, whiteAI, blackAI);
        aiMatch.setDelayBetweenMoves(1500); // 1.5 ثانية بين الحركات

        // callback عند انتهاء المباراة
        aiMatch.setOnGameOver(() -> {
            updateUIFromController();
            statusBar.setStatus("مباراة ذكاء اصطناعي - انتهت اللعبة: " + gameInstance.getGameResult());
        });

        // callback لتنفيذ كل حركة مع الرسوم المتحركة
        aiMatch.setOnMoveExecuted((move) -> {
            controller.executeMoveWithAnimation(move);
        });

        // بدء المباراة
        new Thread(() -> aiMatch.startMatch()).start();
    }

    /**
     * إيقاف مباراة ذكاء اصطناعي حالية
     */
    public void stopAIVsAIMatch() {
        if (aiMatch != null) {
            aiMatch.stopMatch();
            aiMatch = null;
        }
    }

    /**
     * التحقق من ما إذا كانت مباراة ذكاء اصطناعي قيد التشغيل
     */
    public boolean isAIVsAIMatchRunning() {
        return aiMatch != null && aiMatch.isRunning();
    }
}