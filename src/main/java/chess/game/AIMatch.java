package chess.game;

import chess.model.Move;
import chess.model.PieceColor;
import javafx.application.Platform;

/**
 * 管理两个人工智能之间自动比赛的类。
 * 不需要用户干预即可自动执行移动。
 */
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

    /**
     * 用于以视觉方式执行移动的回调接口
     */
    public interface MoveCallback {
        void executeMove(Move move);
    }

    public AIMatch(Game game, AIPlayer whiteAI, AIPlayer blackAI) {
        this.game = game;
        this.whiteAI = whiteAI;
        this.blackAI = blackAI;
    }

    /**
     * 启动人工智能之间的自动比赛
     */
    public void startMatch() {
        if (isRunning) {
            return;
        }
        isRunning = true;
        isPaused = false;
        moveCount = 0;
        executeNextMove();
    }

    /**
     * 执行相应人工智能的下一步移动
     */
    private void executeNextMove() {
        if (!isRunning || isPaused || game.isGameOver()) {
            if (game.isGameOver() && onGameOver != null) {
                onGameOver.run();
            }
            isRunning = false;
            return;
        }

        if (moveCount >= maxMoves) {
            game.setGameOver(true, "تعادل - تم الوصول للحد الأقصى من الحركات");
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

    /**
     * 在延迟后安排执行下一步移动
     */
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

    /**
     * 暂停比赛
     */
    public void pauseMatch() {
        isPaused = true;
    }

    /**
     * استئناف المباراة
     */
    public void resumeMatch() {
        if (isRunning) {
            isPaused = false;
            executeNextMove();
        }
    }

    /**
     * إيقاف المباراة نهائياً
     */
    public void stopMatch() {
        isRunning = false;
        isPaused = false;
    }

    /**
     * تعيين callback الذي يتم تنفيذه عند انتهاء المباراة
     */
    public void setOnGameOver(Runnable onGameOver) {
        this.onGameOver = onGameOver;
    }

    /**
     * تعيين callback الذي يتم تنفيذه بعد كل حركة
     */
    public void setOnMoveExecuted(MoveCallback onMoveExecuted) {
        this.onMoveExecuted = onMoveExecuted;
    }

    /**
     * تعيين التأخير بين الحركات (بالميلي ثانية)
     */
    public void setDelayBetweenMoves(long delayMs) {
        this.delayBetweenMoves = Math.max(100, delayMs);
    }

    /**
     * الحصول على التأخير بين الحركات
     */
    public long getDelayBetweenMoves() {
        return delayBetweenMoves;
    }

    /**
     * التحقق من ما إذا كانت المباراة قيد التشغيل
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * التحقق من ما إذا كانت المباراة موقوفة مؤقتاً
     */
    public boolean isPaused() {
        return isPaused;
    }

    /**
     * الحصول على عدد الحركات المنفذة
     */
    public int getMoveCount() {
        return moveCount;
    }

    /**
     * تعيين الحد الأقصى للحركات
     */
    public void setMaxMoves(int maxMoves) {
        this.maxMoves = maxMoves;
    }
}
