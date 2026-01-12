package chess.view.components;

import chess.model.Move;
import chess.model.Position;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Gestor de animaciones para el tablero de ajedrez.
 * RESPONSABILIDAD: Ejecutar animaciones de movimientos, capturas, etc.
 * NO VALIDA REGLAS - solo anima basado en órdenes del GameController
 */
public class BoardAnimationManager {
    private StackPane boardContainer;
    private ChessSquare[][] squares;
    private boolean isFlipped = false;

    public BoardAnimationManager(StackPane boardContainer, ChessSquare[][] squares) {
        this.boardContainer = boardContainer;
        this.squares = squares;
    }

    /**
     * Anima un movimiento simple de pieza
     */
    public void animateMove(Move move, Runnable onFinished) {
        ChessSquare fromSquare = getSquare(move.getFrom());
        ChessSquare toSquare = getSquare(move.getTo());

        if (fromSquare == null || toSquare == null) {
            if (onFinished != null)
                onFinished.run();
            return;
        }

        boolean isCapture = !toSquare.isEmpty();

        animateSlidingMovement(fromSquare, toSquare, isCapture, onFinished);
    }

    /**
     * Anima dos movimientos simultáneamente (enroque)
     */
    public void animateMovesSimultaneously(Move move1, Move move2, Runnable onFinished) {
        ChessSquare from1 = getSquare(move1.getFrom());
        ChessSquare to1 = getSquare(move1.getTo());
        ChessSquare from2 = getSquare(move2.getFrom());
        ChessSquare to2 = getSquare(move2.getTo());

        if (from1 == null || to1 == null || from2 == null || to2 == null) {
            if (onFinished != null)
                onFinished.run();
            return;
        }

        javafx.scene.Node node1 = getVisiblePieceNode(from1);
        javafx.scene.Node node2 = getVisiblePieceNode(from2);

        if (node1 == null || node2 == null) {
            if (onFinished != null)
                onFinished.run();
            return;
        }

        FadeTransition fadeOut1 = new FadeTransition(Duration.millis(300), node1);
        FadeTransition fadeOut2 = new FadeTransition(Duration.millis(300), node2);
        fadeOut1.setFromValue(1.0);
        fadeOut1.setToValue(0.0);
        fadeOut2.setFromValue(1.0);
        fadeOut2.setToValue(0.0);

        ParallelTransition parallelFadeOut = new ParallelTransition(fadeOut1, fadeOut2);
        parallelFadeOut.setOnFinished(e -> {
            if (onFinished != null)
                onFinished.run();
        });
        parallelFadeOut.play();
    }

    /**
     * Animación de movimiento suave
     */
    private void animateSlidingMovement(ChessSquare fromSquare, ChessSquare toSquare,
            boolean isCapture, Runnable onFinished) {

        javafx.scene.Node movingPiece = createMovingPiece(fromSquare);
        if (movingPiece == null) {
            if (onFinished != null)
                onFinished.run();
            return;
        }

        boardContainer.getChildren().add(movingPiece);
        hidePieceInSquare(fromSquare);

        double[] fromCoords = getSquareCoordinates(fromSquare);
        double[] toCoords = getSquareCoordinates(toSquare);

        movingPiece.setTranslateX(fromCoords[0]);
        movingPiece.setTranslateY(fromCoords[1]);
        movingPiece.setScaleX(1.1);
        movingPiece.setScaleY(1.1);
        movingPiece.setEffect(new DropShadow(10, Color.BLACK));

        TranslateTransition moveTransition = new TranslateTransition(Duration.millis(400), movingPiece);
        moveTransition.setToX(toCoords[0]);
        moveTransition.setToY(toCoords[1]);
        moveTransition.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);

        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(400), movingPiece);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);

        ParallelTransition moveAnimation = new ParallelTransition(moveTransition, scaleTransition);

        if (isCapture) {
            animateCaptureEffect(toSquare, () -> {
                executeMoveAnimation(moveAnimation, movingPiece, onFinished);
            });
        } else {
            executeMoveAnimation(moveAnimation, movingPiece, onFinished);
        }
    }

    /**
     * Ejecuta la animación de movimiento
     */
    private void executeMoveAnimation(ParallelTransition moveAnimation, javafx.scene.Node movingPiece,
            Runnable onFinished) {
        moveAnimation.setOnFinished(e -> {
            boardContainer.getChildren().remove(movingPiece);
            if (onFinished != null)
                onFinished.run();
        });
        moveAnimation.play();
    }

    /**
     * Efecto de captura
     */
    private void animateCaptureEffect(ChessSquare targetSquare, Runnable onCaptureFinished) {
        javafx.scene.Node pieceNode = getVisiblePieceNode(targetSquare);

        if (pieceNode == null) {
            onCaptureFinished.run();
            return;
        }

        ScaleTransition explode = new ScaleTransition(Duration.millis(200), pieceNode);
        explode.setToX(1.3);
        explode.setToY(1.3);
        explode.setInterpolator(javafx.animation.Interpolator.EASE_OUT);

        RotateTransition rotate = new RotateTransition(Duration.millis(300), pieceNode);
        rotate.setByAngle(360);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(250), pieceNode);
        fadeOut.setDelay(Duration.millis(100));
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        TranslateTransition shake = new TranslateTransition(Duration.millis(100), targetSquare.getRoot());
        shake.setByX(3);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);

        ParallelTransition captureEffect = new ParallelTransition(explode, rotate, fadeOut, shake);
        captureEffect.setOnFinished(e -> {
            pieceNode.setScaleX(1.0);
            pieceNode.setScaleY(1.0);
            pieceNode.setRotate(0);
            pieceNode.setOpacity(1.0);
            targetSquare.getRoot().setTranslateX(0);
            targetSquare.getRoot().setTranslateY(0);
            onCaptureFinished.run();
        });

        captureEffect.play();
    }

    /**
     * Crea una copia de la pieza para animar
     */
    private javafx.scene.Node createMovingPiece(ChessSquare fromSquare) {
        double squareSize = Math.min(fromSquare.getRoot().getWidth(), fromSquare.getRoot().getHeight());
        if (squareSize <= 0)
            squareSize = 60.0;

        if (fromSquare.isUsingImages() && fromSquare.getPieceImageView().isVisible()) {
            javafx.scene.image.ImageView copy = new javafx.scene.image.ImageView(
                    fromSquare.getPieceImageView().getImage());
            copy.setFitWidth(squareSize * 0.82);
            copy.setFitHeight(squareSize * 0.82);
            copy.setPreserveRatio(true);
            return copy;
        } else if (fromSquare.getPieceLabel().isVisible()) {
            javafx.scene.control.Label copy = new javafx.scene.control.Label(fromSquare.getPieceLabel().getText());
            copy.setStyle(fromSquare.getPieceLabel().getStyle());
            return copy;
        }
        return null;
    }

    /**
     * Obtiene el nodo visible de pieza (imagen o label)
     */
    private javafx.scene.Node getVisiblePieceNode(ChessSquare square) {
        if (square.isUsingImages() && square.getPieceImageView().isVisible()) {
            return square.getPieceImageView();
        } else if (square.getPieceLabel().isVisible()) {
            return square.getPieceLabel();
        }
        return null;
    }

    /**
     * Oculta la pieza en la casilla durante la animación
     */
    private void hidePieceInSquare(ChessSquare square) {
        if (square.isUsingImages() && square.getPieceImageView().isVisible()) {
            square.getPieceImageView().setVisible(false);
        } else if (square.getPieceLabel().isVisible()) {
            square.getPieceLabel().setVisible(false);
        }
    }

    /**
     * Obtiene coordenadas del centro de una casilla
     */
    private double[] getSquareCoordinates(ChessSquare square) {
        Position pos = square.getPosition();
        double squareSize = Math.min(square.getRoot().getWidth(), square.getRoot().getHeight());
        if (squareSize <= 0)
            squareSize = 60.0;
        double boardSize = squareSize * 8.0;
        double boardCenterX = boardSize / 2.0;
        double boardCenterY = boardSize / 2.0;

        int displayCol = isFlipped ? 7 - pos.getCol() : pos.getCol();
        int displayRow = isFlipped ? 7 - pos.getRow() : pos.getRow();
        double x = (displayCol * squareSize) - boardCenterX + (squareSize / 2);
        double y = (displayRow * squareSize) - boardCenterY + (squareSize / 2);

        return new double[] { x, y };
    }

    /**
     * Obtiene una casilla por posición
     */
    private ChessSquare getSquare(Position pos) {
        if (isValidPosition(pos)) {
            return squares[pos.getRow()][pos.getCol()];
        }
        return null;
    }

    /**
     * Valida que la posición esté dentro del tablero
     */
    private boolean isValidPosition(Position pos) {
        return pos != null && pos.getRow() >= 0 && pos.getRow() < 8 &&
                pos.getCol() >= 0 && pos.getCol() < 8;
    }

    /**
     * Establece la orientación del tablero
     */
    public void setFlipped(boolean flipped) {
        this.isFlipped = flipped;
    }
}
