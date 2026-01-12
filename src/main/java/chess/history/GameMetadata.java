package chess.history;

import java.io.Serializable;

/**
 * Metadata about a saved game, including the game mode.
 */
public class GameMetadata implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum GameMode {
        PVP,    
        PVAI,   
        AIVAI   
    }

    private final GameMode gameMode;
    private final long whiteTimeMillis;
    private final long blackTimeMillis;

    public GameMetadata(GameMode gameMode) {
        this.gameMode = gameMode;
        this.whiteTimeMillis = 0;
        this.blackTimeMillis = 0;
    }

    public GameMetadata(GameMode gameMode, long whiteTimeMillis, long blackTimeMillis) {
        this.gameMode = gameMode;
        this.whiteTimeMillis = whiteTimeMillis;
        this.blackTimeMillis = blackTimeMillis;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public long getWhiteTimeMillis() {
        return whiteTimeMillis;
    }

    public long getBlackTimeMillis() {
        return blackTimeMillis;
    }
}
