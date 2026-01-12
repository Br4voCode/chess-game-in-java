package chess.game;

import chess.model.Move;
import chess.model.PieceColor;
import javafx.application.Platform;


public class AIMatch {
    private Game game;
    private AIPlayer whiteAI;
    private AIPlayer blackAI;
    private boolean isRunning = false;
    private boolean isPaused = false;
    private Runnable onGameOver;
    private MoveCallback onMoveExecuted;
    private long delayBetweenMoves = 1000;
    private int moveCount = 0;
    private int maxMoves = 200;

    public interface MoveCallback {
        void executeMove(Move move);
    }

    public AIMatch(Game game, AIPlayer whiteAI, AIPlayer blackAI) {
        this.game = game;
        this.whiteAI = whiteAI;
        this.blackAI = blackAI;
    }


    public void startMatch() {
        if (isRunning) {
            return;
        }
        isRunning = true;
        isPaused = false;
        moveCount = 0;
        executeNextMove();
    }


    private void executeNextMove() {
        if (!isRunning || isPaused || game.isGameOver()) {
            if (game.isGameOver() && onGameOver != null) {
                onGameOver.run();
            }
            isRunning = false;
            return;
        }

        if (moveCount >= maxMoves) {
            game.setGameOver(true, "Empate - Se ha alcanzado el número máximo de movimientos");
            if (onGameOver != null) {
                onGameOver.run();
            }
            isRunning = false;
            return;
        }

        try {
            PieceColor currentTurn = game.getTurn();
            AIPlayer currentAI = (currentTurn == PieceColor.WHITE) ? whiteAI : blackAI;

            Move move = currentAI.chooseMove(game.getBoard());

            if (move != null) {
                moveCount++;

                if (onMoveExecuted != null) {

                    Platform.runLater(() -> {
                        onMoveExecuted.executeMove(move);

                        scheduleNextMove();
                    });
                } else {

                    if (!game.applyMove(move)) {

                        if (game.isGameOver()) {
                            if (onGameOver != null) {
                                Platform.runLater(onGameOver);
                            }
                            isRunning = false;
                            return;
                        }
                    }

                    scheduleNextMove();
                }
            } else {

                if (game.isGameOver()) {
                    if (onGameOver != null) {
                        Platform.runLater(onGameOver);
                    }
                    isRunning = false;
                    return;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            isRunning = false;
        }
    }


    private void scheduleNextMove() {
        new Thread(() -> {
            try {
                Thread.sleep(delayBetweenMoves);
                if (isRunning && !isPaused) {
                    Platform.runLater(this::executeNextMove);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }


    public void pauseMatch() {
        isPaused = true;
    }


    public void resumeMatch() {
        if (isRunning) {
            isPaused = false;
            executeNextMove();
        }
    }


    public void stopMatch() {
        isRunning = false;
        isPaused = false;
    }


    public void setOnGameOver(Runnable onGameOver) {
        this.onGameOver = onGameOver;
    }


    public void setOnMoveExecuted(MoveCallback onMoveExecuted) {
        this.onMoveExecuted = onMoveExecuted;
    }


    public void setDelayBetweenMoves(long delayMs) {
        this.delayBetweenMoves = Math.max(100, delayMs);
    }


    public long getDelayBetweenMoves() {
        return delayBetweenMoves;
    }


    public boolean isRunning() {
        return isRunning;
    }

    public boolean isPaused() {
        return isPaused;
    }


    public int getMoveCount() {
        return moveCount;
    }

    public void setMaxMoves(int maxMoves) {
        this.maxMoves = maxMoves;
    }
}
