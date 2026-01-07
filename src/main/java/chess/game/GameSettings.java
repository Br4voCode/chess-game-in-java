package chess.game;

import java.io.Serializable;

import chess.model.PieceColor;

/**
 * Represents lightweight configuration for the last launched game mode.
 */
public class GameSettings implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int MIN_DEPTH = 1;
    public static final int MAX_DEPTH = 8;
    public static final int DEFAULT_DEPTH = 3;

    private final PieceColor humanColor;
    private final int aiDepth;
    private final boolean playerVsPlayer;
    private final boolean aiVsAI;

    private GameSettings(PieceColor humanColor, int aiDepth, boolean playerVsPlayer, boolean aiVsAI) {
        this.humanColor = humanColor != null ? humanColor : PieceColor.WHITE;
        this.aiDepth = clampDepth(aiDepth);
        this.playerVsPlayer = playerVsPlayer;
        this.aiVsAI = aiVsAI;
    }

    public static GameSettings humanVsAI(PieceColor color, int depth) {
        return new GameSettings(color, depth, false, false);
    }

    public static GameSettings twoPlayers() {
        return new GameSettings(PieceColor.WHITE, DEFAULT_DEPTH, true, false);
    }

    public static GameSettings aiVsAi(int depth) {
        return new GameSettings(PieceColor.WHITE, depth, false, true);
    }

    public PieceColor getHumanColor() {
        return humanColor;
    }

    public int getAiDepth() {
        return aiDepth;
    }

    public boolean isPlayerVsPlayer() {
        return playerVsPlayer;
    }

    public boolean isAIVsAI() {
        return aiVsAI;
    }

    public boolean isHumanVsAI() {
        return !playerVsPlayer && !aiVsAI;
    }

    private int clampDepth(int depth) {
        if (depth < MIN_DEPTH) {
            return MIN_DEPTH;
        }
        if (depth > MAX_DEPTH) {
            return MAX_DEPTH;
        }
        return depth;
    }
}
