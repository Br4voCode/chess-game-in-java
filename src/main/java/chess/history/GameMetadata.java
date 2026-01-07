package chess.history;

import java.io.Serializable;

/**
 * Metadata about a saved game, including the game mode.
 */
public class GameMetadata implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum GameMode {
        PVP,    // Player vs Player
        PVAI,   // Player vs AI
        AIVAI   // AI vs AI
    }

    private final GameMode gameMode;

    public GameMetadata(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public GameMode getGameMode() {
        return gameMode;
    }
}
