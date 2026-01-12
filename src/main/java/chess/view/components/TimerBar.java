package chess.view.components;

import chess.model.pieces.PieceColor;
import chess.model.GameClock;
import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Display component for showing player timer at top or bottom of board
 * Shows player name/icon on left and timer on right
 */
public class TimerBar extends VBox {
    private GameClock gameClock;
    private PieceColor playerColor;
    private Label timerLabel;
    private Label playerLabel;
    private HBox mainContainer;
    private AnimationTimer animationTimer;
    private boolean isTopBar; // true for white (top), false for black (bottom)

    /**
     * Constructor simplificado para uso sin GameClock
     */
    public TimerBar(PieceColor playerColor) {
        this(null, playerColor, false);
    }

    public TimerBar(GameClock gameClock, PieceColor playerColor, boolean isTopBar) {
        this.gameClock = gameClock;
        this.playerColor = playerColor;
        this.isTopBar = isTopBar;
        
        initializeUI();
        startTimerUpdates();
    }

    private void initializeUI() {
        this.setStyle("-fx-padding: 8; -fx-background-color: transparent; -fx-background-radius: 10; -fx-border-radius: 10;");
        this.setMaxHeight(44);
        this.setMinHeight(44);
        // Los relojes usarán el ancho que se les bindee (desde GameView)
        this.setMaxWidth(Double.MAX_VALUE);

        mainContainer = new HBox(10);
        mainContainer.setAlignment(Pos.CENTER_LEFT);
        mainContainer.setMaxWidth(Double.MAX_VALUE);

        // Left side: Player icon and name
        HBox playerInfo = createPlayerInfoBox();

        // Center spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Right side: Timer
        timerLabel = new Label("05:00");
        timerLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white; -fx-font-family: 'Courier New';");
        timerLabel.setMinWidth(84);

        mainContainer.getChildren().addAll(playerInfo, spacer, timerLabel);
        this.getChildren().add(mainContainer);
    }

    private HBox createPlayerInfoBox() {
        HBox playerBox = new HBox(10);
        playerBox.setAlignment(Pos.CENTER_LEFT);

        // Player icon/avatar (simple colored circle)
        VBox iconBox = new VBox();
        iconBox.setStyle(String.format(
            "-fx-background-color: %s; -fx-background-radius: 25; " +
            "-fx-min-width: 32; -fx-max-width: 32; " +
            "-fx-min-height: 32; -fx-max-height: 32;",
            playerColor == PieceColor.WHITE ? "#bbbbbb" : "#2b2b2b"
        ));
        iconBox.setAlignment(Pos.CENTER);

        Label playerIcon = new Label(playerColor == PieceColor.WHITE ? "♔" : "♚");
        playerIcon.setStyle("-fx-font-size: 16px; -fx-text-fill: " +
            (playerColor == PieceColor.WHITE ? "#2b2b2b" : "white") + ";");
        iconBox.getChildren().add(playerIcon);

        // Player name
        String playerName = playerColor == PieceColor.WHITE ? "White" : "Black";
        playerLabel = new Label(playerName);
        playerLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: white;");

        playerBox.getChildren().addAll(iconBox, playerLabel);
        return playerBox;
    }

    /**
     * Start updating the timer display
     */
    private void startTimerUpdates() {
        animationTimer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                // Update every 100ms to smooth animation
                if (now - lastUpdate >= 100_000_000) { // 100ms in nanoseconds
                    updateTimerDisplay();
                    lastUpdate = now;
                }
            }
        };
        animationTimer.start();
    }

    /**
     * Update the displayed timer
     */
    public void updateTimerDisplay() {
        if (gameClock != null) {
            String timeStr = gameClock.getFormattedTime(playerColor);
            timerLabel.setText(timeStr);

            long timeRemaining = gameClock.getTimeRemaining(playerColor);
            if (timeRemaining <= 0) {
                timerLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #bbbbbb; -fx-font-family: 'Courier New';");
            } else {
                timerLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white; -fx-font-family: 'Courier New';");
            }

            // Highlight if it's this player's turn
            boolean isActive = gameClock.getActivePlayer() == playerColor;
            if (isActive && gameClock.isRunning() && !gameClock.isPaused()) {
                this.setStyle("-fx-padding: 8; -fx-background-color: transparent; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #4caf50; -fx-border-width: 2;");
            } else {
                this.setStyle("-fx-padding: 8; -fx-background-color: transparent; -fx-background-radius: 10; -fx-border-radius: 10;");
            }
        }
    }

    /**
     * Update the game clock reference
     */
    public void setGameClock(GameClock gameClock) {
        this.gameClock = gameClock;
    }

    /**
     * Stop the timer animation
     */
    public void stop() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }

    /**
     * Reset the timer display
     */
    public void reset() {
        if (gameClock != null) {
            timerLabel.setText(gameClock.getFormattedTime(playerColor));
        }
    }
}
