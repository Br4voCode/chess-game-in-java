package chess.view;

import chess.controller.GameController;
import chess.game.AIMatch;
import chess.game.AIPlayer;
import chess.game.Game;
import chess.game.GameReconstructor;
import chess.game.GameSettings;
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

    private Label turnValue;
    private Label gameStateValue;
    private FlowPane whiteCapturedBox;
    private FlowPane blackCapturedBox;
    private VBox movesBox;

    private TimerBar blackTimerBar;
    private TimerBar whiteTimerBar;
    private Game gameInstance;
    private boolean shouldLoadHistory;
    private boolean shouldStartAIVsAI;
    private AIMatch aiMatch;
    private PieceColor humanPlayerColor = PieceColor.WHITE;
    private int aiSearchDepth = 3;

    private Button undoButton;
    private Button redoButton;

    public GameView() {
        this(GameSettings.humanVsAI(PieceColor.WHITE, GameSettings.DEFAULT_DEPTH));
    }

    public GameView(PieceColor humanColor, int aiDepth) {
        this(GameSettings.humanVsAI(humanColor, aiDepth));
    }

    public GameView(GameSettings settings) {
        this(false, settings);
    }

    public GameView(boolean loadFromHistory, GameSettings settings) {
        this(loadFromHistory,
                settings != null && settings.isPlayerVsPlayer(),
                settings != null && settings.isAIVsAI(),
                resolveHumanColor(settings),
                resolveDepth(settings));
    }

    public GameView(boolean loadFromHistory) {
        this(loadFromHistory, GameSettings.humanVsAI(PieceColor.WHITE, GameSettings.DEFAULT_DEPTH));
    }

    public GameView(boolean loadFromHistory, boolean isPlayerVsPlayer) {
        this(loadFromHistory, isPlayerVsPlayer, false, PieceColor.WHITE, GameSettings.DEFAULT_DEPTH);
    }

    public GameView(boolean loadFromHistory, boolean isPlayerVsPlayer, boolean isAIVsAI) {
        this(loadFromHistory, isPlayerVsPlayer, isAIVsAI, PieceColor.WHITE, GameSettings.DEFAULT_DEPTH);
    }

    private static PieceColor resolveHumanColor(GameSettings settings) {
        if (settings == null || settings.isPlayerVsPlayer() || settings.isAIVsAI()) {
            return PieceColor.WHITE;
        }
        return settings.getHumanColor();
    }

    private static int resolveDepth(GameSettings settings) {
        if (settings == null) {
            return GameSettings.DEFAULT_DEPTH;
        }
        return settings.getAiDepth();
    }

    private GameView(boolean loadFromHistory, boolean isPlayerVsPlayer, boolean isAIVsAI,
            PieceColor humanColor, int aiDepth) {
        this.shouldLoadHistory = loadFromHistory;
        this.humanPlayerColor = humanColor != null ? humanColor : PieceColor.WHITE;
        this.aiSearchDepth = Math.max(1, Math.min(8, aiDepth));
        this.shouldStartAIVsAI = false;
        initializeComponents(loadFromHistory, isPlayerVsPlayer, isAIVsAI, this.humanPlayerColor, this.aiSearchDepth);
        setupLayout();
        updateUIFromController();

        if (shouldLoadHistory && gameInstance != null && StartScreen.hasGameHistory()) {
            loadStepHistoryToUI(gameInstance.getStepHistory());
        }

        // Iniciar partida IA vs IA si es necesario (tanto para nuevas como cargadas)
        if (shouldStartAIVsAI) {
            startAIVsAIMatch();
        } else if (!isPlayerVsPlayer && controller != null && this.humanPlayerColor == PieceColor.BLACK) {
            controller.triggerAIMoveIfNeeded();
        }

        // Si cargamos una partida PvAI y es el turno de la IA, activar su jugada
        if (loadFromHistory && !isPlayerVsPlayer && !isAIVsAI && gameInstance != null) {
            triggerAITurnIfNeeded();
        }
    }

    private void initializeComponents(boolean loadFromHistory, boolean isPlayerVsPlayer, boolean isAIVsAI,
            PieceColor humanColor, int aiDepth) {
        Game game;
        boolean shouldStartAIMatch = false;
        int depth = Math.max(1, Math.min(8, aiDepth));

        if (loadFromHistory && StartScreen.hasGameHistory()) {
            chess.history.GameMetadata.GameMode savedMode = chess.game.GameReconstructor.getGameMode(StartScreen.getHistoryFilePath());
            
            if (savedMode == chess.history.GameMetadata.GameMode.PVP) {
                // Partida guardada era PvP
                game = chess.game.GameReconstructor.reconstructGameFromHistory(
                        StartScreen.getHistoryFilePath(),
                        new HumanPlayer(),
                        new HumanPlayer());
                isPlayerVsPlayer = true;
                isAIVsAI = false;
            } else if (savedMode == chess.history.GameMetadata.GameMode.AIVAI) {
                // Partida guardada era AIvAI
                game = chess.game.GameReconstructor.reconstructGameFromHistory(
                        StartScreen.getHistoryFilePath(),
                        new AIPlayer(PieceColor.WHITE, 3),
                        new AIPlayer(PieceColor.BLACK, 3));
                isPlayerVsPlayer = false;
                isAIVsAI = true;
                shouldStartAIMatch = true;
            } else {
                // Partida guardada era PvAI o no tiene metadata (default)
                game = chess.game.GameReconstructor.reconstructGameFromHistory(
                        StartScreen.getHistoryFilePath(),
                        new HumanPlayer(),
                        new AIPlayer(PieceColor.BLACK, 3));
                isPlayerVsPlayer = false;
                isAIVsAI = false;
            }
            if (isAIVsAI) {
                game = GameReconstructor.reconstructGameFromHistory(
                        StartScreen.getHistoryFilePath(),
                        new AIPlayer(PieceColor.WHITE, depth),
                        new AIPlayer(PieceColor.BLACK, depth));
            } else if (isPlayerVsPlayer) {
                game = GameReconstructor.reconstructGameFromHistory(
                        StartScreen.getHistoryFilePath(),
                        new HumanPlayer(),
                        new HumanPlayer());
            } else {
                if (humanColor == PieceColor.WHITE) {
                    game = GameReconstructor.reconstructGameFromHistory(
                            StartScreen.getHistoryFilePath(),
                            new HumanPlayer(),
                            new AIPlayer(PieceColor.BLACK, depth));
                } else {
                    game = GameReconstructor.reconstructGameFromHistory(
                            StartScreen.getHistoryFilePath(),
                            new AIPlayer(PieceColor.WHITE, depth),
                            new HumanPlayer());
                }
            }
        } else {

            if (isAIVsAI) {

                game = new Game(new AIPlayer(PieceColor.WHITE, depth), new AIPlayer(PieceColor.BLACK, depth));
                game.setGameMode(chess.history.GameMetadata.GameMode.AIVAI);
                shouldStartAIMatch = true;
            } else if (isPlayerVsPlayer) {

                game = new Game(new HumanPlayer(), new HumanPlayer());
                game.setGameMode(chess.history.GameMetadata.GameMode.PVP);
            } else {
                if (humanColor == PieceColor.WHITE) {
                    game = new Game(new HumanPlayer(), new AIPlayer(PieceColor.BLACK, depth));
                } else {
                    game = new Game(new AIPlayer(PieceColor.WHITE, depth), new HumanPlayer());
                }
                game.setGameMode(chess.history.GameMetadata.GameMode.PVAI);
            }
        }

        this.gameInstance = game;
        this.chessBoard = new ChessBoard();
        PieceColor bottomColor = PieceColor.WHITE;
        if (!isPlayerVsPlayer && !isAIVsAI) {
            bottomColor = humanColor;
        }
        this.chessBoard.setBottomColor(bottomColor);
        this.statusBar = new StatusBar();
        this.controller = new GameController(game, chessBoard, statusBar, isPlayerVsPlayer, isAIVsAI);
        this.controller.setGameView(this);

        this.chessBoard.setSquareClickListener(controller::onSquareClicked);

        // Store flag to start AI match after UI is set up
        this.shouldStartAIVsAI = shouldStartAIMatch;

    }

    private void setupLayout() {
        root = new BorderPane();

        blackTimerBar = new TimerBar(gameInstance.getGameClock(), PieceColor.BLACK, true);
        whiteTimerBar = new TimerBar(gameInstance.getGameClock(), PieceColor.WHITE, false);

        Label title = new Label("♔ Chess Game ♚");
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

        Button hintButton = new Button("Hint");
        hintButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-font-weight: bold;");
        hintButton.setMaxWidth(Double.MAX_VALUE);
        hintButton.setOnAction(e -> {
            controller.hintButton();
        });

        HBox navBox = new HBox(10);
        navBox.setAlignment(Pos.CENTER);

        undoButton = new Button("←");
        undoButton.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-font-weight: bold;");
        undoButton.setDisable(true);
        undoButton.setOnAction(e -> controller.undoStepWithAnimation());

        redoButton = new Button("→");
        redoButton.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-font-weight: bold;");
        redoButton.setDisable(true);
        redoButton.setOnAction(e -> controller.redoStepWithAnimation());

        navBox.getChildren().addAll(undoButton, redoButton);

        if (controller != null && gameInstance != null && gameInstance.getStepHistory() != null) {
            boolean canUndo = gameInstance.getStepHistory().canUndo();
            boolean canRedo = gameInstance.getStepHistory().canRedo();
            setHistoryNavigationEnabled(canUndo, canRedo);
        }

        Label turnLabel = new Label("Current turn:");
        turnLabel.setStyle("-fx-text-fill: #bbbbbb;");

        turnValue = new Label(gameInstance.getTurn() == PieceColor.WHITE ? "White" : "Black");
        turnValue.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label gameStateLabel = new Label("Game state:");
        gameStateLabel.setStyle("-fx-text-fill: #bbbbbb;");

        gameStateValue = new Label("In progress");
        gameStateValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #4caf50;");
        Pane spacer = new Pane();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        panel.getChildren().addAll(
                panelTitle,
                newGameButton,
                navBox,
                hintButton,
                new Pane(),
                turnLabel, turnValue,
                spacer,
                gameStateLabel, gameStateValue);

        return panel;
    }

    /**
     * Called by the controller to toggle navigation buttons.
     */
    public void setHistoryNavigationEnabled(boolean canUndo, boolean canRedo) {
        if (undoButton != null) {
            undoButton.setDisable(!canUndo);
        }
        if (redoButton != null) {
            redoButton.setDisable(!canRedo);
        }
    }

    private VBox createRightPanel() {
        VBox panel = new VBox(10);
        panel.setPrefWidth(200);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #3c3f41; -fx-background-radius: 5;");
        panel.setMaxHeight(Double.MAX_VALUE);

        Label panelTitle = new Label("Game Info");
        panelTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

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

        StackPane boardWithAnimations = chessBoard.getBoardWithAnimations();

        boardWithAnimations.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        Label helpLabel = new Label("Click a piece to select it, then click a destination square");
        helpLabel.setStyle("-fx-text-fill: #bbbbbb; -fx-font-size: 12px;");

        VBox boardContainer = new VBox(10);
        boardContainer.setAlignment(Pos.CENTER);
        boardContainer.setMaxHeight(Double.MAX_VALUE);
        boardContainer.setMaxWidth(Double.MAX_VALUE);
        boardContainer.setPadding(new Insets(10));
        boardContainer.setStyle("-fx-background-color: #2b2b2b;");
        VBox.setVgrow(boardContainer, Priority.ALWAYS);

        VBox mainBoardVBox = new VBox(10);
        mainBoardVBox.setAlignment(Pos.CENTER);
        mainBoardVBox.setMaxHeight(Double.MAX_VALUE);
        mainBoardVBox.setMaxWidth(Double.MAX_VALUE);
        mainBoardVBox.setStyle("-fx-background-color: #2b2b2b;");
        VBox.setVgrow(mainBoardVBox, Priority.ALWAYS);

        HBox topTimerContainer = new HBox();
        topTimerContainer.setAlignment(Pos.CENTER);
        if (blackTimerBar != null) {
            blackTimerBar.prefWidthProperty().bind(chessBoard.getBoard().widthProperty());
            topTimerContainer.getChildren().add(blackTimerBar);
        }

        VBox.setVgrow(boardWithAnimations, Priority.ALWAYS);

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

            if (movesBox.getChildren().size() > 10) {
                movesBox.getChildren().remove(0);
            }
        }
    }

    /**
     * Removes the last rendered move from the move history panel.
     * Used for undo navigation.
     */
    public void removeLastMoveFromHistory() {
        if (movesBox == null) {
            return;
        }
        int size = movesBox.getChildren().size();
        if (size > 0) {
            movesBox.getChildren().remove(size - 1);
        }
    }

    /**
     * Removes the last captured piece icon from the UI.
     *
     * @param capturedPieceColor the color of the piece that was captured on the
     *                           board
     */
    public void removeLastCapturedPiece(chess.model.PieceColor capturedPieceColor) {
        if (capturedPieceColor == null) {
            return;
        }

        FlowPane targetBox = (capturedPieceColor == PieceColor.WHITE) ? blackCapturedBox : whiteCapturedBox;
        if (targetBox == null) {
            return;
        }
        int size = targetBox.getChildren().size();
        if (size > 0) {
            targetBox.getChildren().remove(size - 1);
        }
    }

    private void loadStepHistoryToUI(chess.history.StepHistory history) {
        java.util.List<chess.history.Step> steps = history.getAppliedSteps();
        for (chess.history.Step step : steps) {
            addMoveToHistoryWithColor(step.getDisplayText(), step.getMoverColor());
        }
    }

    /**
     * Trigger AI move if it's the AI's turn after loading a game
     */
    private void triggerAITurnIfNeeded() {
        if (gameInstance == null || controller == null) {
            return;
        }

        // Check if current turn belongs to AI player
        PieceColor currentTurn = gameInstance.getTurn();
        chess.game.Player currentPlayer = (currentTurn == PieceColor.WHITE) 
            ? gameInstance.getWhitePlayer() 
            : gameInstance.getBlackPlayer();

        // If it's AI's turn, trigger the AI move with a small delay
        if (currentPlayer instanceof AIPlayer) {
            new Thread(() -> {
                try {
                    Thread.sleep(1000); // Small delay to let UI settle
                    javafx.application.Platform.runLater(() -> {
                        chess.model.Move aiMove = gameInstance.getAIMoveIfAny();
                        if (aiMove != null) {
                            controller.executeMoveWithAnimation(aiMove);
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
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
        aiMatch.setDelayBetweenMoves(1500);

        aiMatch.setOnGameOver(() -> {
            updateUIFromController();
            statusBar.setStatus("مباراة ذكاء اصطناعي - انتهت اللعبة: " + gameInstance.getGameResult());
            controller.onAIMatchGameOver();
        });

        aiMatch.setOnMoveExecuted((move) -> {
            controller.executeMoveWithAnimation(move);
        });

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