package chess.model;

import java.io.Serializable;
import chess.model.pieces.PieceColor;

/**
 * Manages game clock for chess with independent timers for each player
 */
public class GameClock implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Initial time in milliseconds (5 minutes = 300_000 ms)
    private static final long INITIAL_TIME_MILLIS = 5 * 60 * 1000;
    
    // Time remaining for each player (in milliseconds)
    private long whiteTimeRemainingMillis;
    private long blackTimeRemainingMillis;
    
    // Track which player is currently active
    private PieceColor activePlayer;
    
    // Timestamp for timing (milliseconds)
    private long lastUpdateTimeMillis;
    private boolean isRunning = false;
    
    // Flag to track if game is paused/stopped
    private boolean clockPaused = false;

    public GameClock() {
        this.whiteTimeRemainingMillis = INITIAL_TIME_MILLIS;
        this.blackTimeRemainingMillis = INITIAL_TIME_MILLIS;
        this.activePlayer = PieceColor.WHITE;
        this.lastUpdateTimeMillis = System.currentTimeMillis();
    }

    /**
     * Initialize clock with custom initial time in seconds
     */
    public GameClock(long initialTimeSeconds) {
        long initialTimeMillis = Math.max(0, initialTimeSeconds) * 1000;
        this.whiteTimeRemainingMillis = initialTimeMillis;
        this.blackTimeRemainingMillis = initialTimeMillis;
        this.activePlayer = PieceColor.WHITE;
        this.lastUpdateTimeMillis = System.currentTimeMillis();
    }

    /**
     * Start the clock - should be called when a move is made
     */
    public void start() {
        this.lastUpdateTimeMillis = System.currentTimeMillis();
        this.isRunning = true;
    }

    /**
     * Stop the clock - should be called when game ends
     */
    public void stop() {
        updateCurrentPlayerTime();
        this.isRunning = false;
    }

    /**
     * Pause the clock without losing time
     */
    public void pause() {
        if (isRunning) {
            updateCurrentPlayerTime();
        }
        this.clockPaused = true;
    }

    /**
     * Resume the clock
     */
    public void resume() {
        this.lastUpdateTimeMillis = System.currentTimeMillis();
        this.clockPaused = false;
    }

    /**
     * Switch active player and update timers
     * This should be called after each move
     */
    public void switchPlayer() {
        if (!isRunning || clockPaused) return;
        
        // Update current player's remaining time
        updateCurrentPlayerTime();
        
        // Switch to next player
        activePlayer = activePlayer == PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE;
        
        // Reset last update time for new player
        this.lastUpdateTimeMillis = System.currentTimeMillis();
    }

    /**
     * Update the current active player's remaining time
     */
    private void updateCurrentPlayerTime() {
        if (!isRunning || clockPaused) return;

        long currentTimeMillis = System.currentTimeMillis();
        long elapsedMillis = currentTimeMillis - lastUpdateTimeMillis;
        if (elapsedMillis <= 0) {
            return;
        }

        if (activePlayer == PieceColor.WHITE) {
            whiteTimeRemainingMillis = Math.max(0, whiteTimeRemainingMillis - elapsedMillis);
        } else {
            blackTimeRemainingMillis = Math.max(0, blackTimeRemainingMillis - elapsedMillis);
        }

        lastUpdateTimeMillis = currentTimeMillis;
    }

    /**
     * Remaining milliseconds for a player.
     * If the clock is running and this is the active player, this value is calculated in real time.
     */
    private long getTimeRemainingMillis(PieceColor player) {
        long baseMillis = (player == PieceColor.WHITE) ? whiteTimeRemainingMillis : blackTimeRemainingMillis;
        if (!isRunning || clockPaused || player != activePlayer) {
            return baseMillis;
        }

        long nowMillis = System.currentTimeMillis();
        long elapsedMillis = nowMillis - lastUpdateTimeMillis;
        if (elapsedMillis <= 0) {
            return baseMillis;
        }
        return Math.max(0, baseMillis - elapsedMillis);
    }

    /**
     * Get the remaining time for a player in seconds (rounded up to avoid showing 04:59 immediately).
     */
    public long getTimeRemaining(PieceColor player) {
        long millis = getTimeRemainingMillis(player);
        return (millis + 999) / 1000;
    }

    /**
     * Get the time remaining for the active player in seconds.
     */
    public long getActivePlayerTimeRemaining() {
        return getTimeRemaining(activePlayer);
    }

    /**
     * Check if a player has run out of time
     */
    public boolean hasTimeExpired(PieceColor player) {
        return getTimeRemainingMillis(player) <= 0;
    }

    /**
     * Get the active player
     */
    public PieceColor getActivePlayer() {
        return activePlayer;
    }

    /**
     * Set the active player (useful for loading saved games)
     */
    public void setActivePlayer(PieceColor player) {
        this.activePlayer = player;
        this.lastUpdateTimeMillis = System.currentTimeMillis();
    }

    /**
     * Format time for display (MM:SS)
     */
    public static String formatTime(long seconds) {
        long minutes = seconds / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    /**
     * Check if clock is running
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Check if clock is paused
     */
    public boolean isPaused() {
        return clockPaused;
    }

    /**
     * Reset clock to initial time
     */
    public void reset() {
        this.whiteTimeRemainingMillis = INITIAL_TIME_MILLIS;
        this.blackTimeRemainingMillis = INITIAL_TIME_MILLIS;
        this.activePlayer = PieceColor.WHITE;
        this.lastUpdateTimeMillis = System.currentTimeMillis();
        this.isRunning = false;
        this.clockPaused = false;
    }

    /**
     * Reset with custom initial time
     */
    public void reset(long initialTimeSeconds) {
        long initialTimeMillis = Math.max(0, initialTimeSeconds) * 1000;
        this.whiteTimeRemainingMillis = initialTimeMillis;
        this.blackTimeRemainingMillis = initialTimeMillis;
        this.activePlayer = PieceColor.WHITE;
        this.lastUpdateTimeMillis = System.currentTimeMillis();
        this.isRunning = false;
        this.clockPaused = false;
    }

    /**
     * Get formatted time for display
     */
    public String getFormattedTime(PieceColor player) {
        long timeRemainingSeconds = getTimeRemaining(player);
        if (timeRemainingSeconds <= 0) {
            return "00:00";
        }
        return formatTime(timeRemainingSeconds);
    }

    @Override
    public String toString() {
        return String.format("GameClock{white=%s, black=%s, active=%s}", 
            formatTime(getTimeRemaining(PieceColor.WHITE)),
            formatTime(getTimeRemaining(PieceColor.BLACK)),
            activePlayer);
    }
}
