package chess.view;

import java.util.List;
import java.util.function.Consumer;

import chess.model.Board;
import chess.model.Move;
import chess.model.Piece;
import chess.model.PieceColor;
import chess.model.Position;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Componente que representa el tablero de ajedrez con animaciones
 */
public class ChessBoard {
	private GridPane board;
	private ChessSquare[][] squares;
	private Consumer<Position> squareClickListener;
	private Board currentBoard;
	private StackPane boardContainer;
    private boolean isFlipped = false;

	public ChessBoard() {
		initializeBoard();
	}

	private void initializeBoard() {
		boardContainer = new StackPane();
		board = new GridPane();
		squares = new ChessSquare[8][8];

		board.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		boardContainer.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

		board.getColumnConstraints().clear();
		board.getRowConstraints().clear();
		for (int i = 0; i < 8; i++) {
			ColumnConstraints cc = new ColumnConstraints();
			cc.setPercentWidth(12.5);
			cc.setHgrow(Priority.ALWAYS);
			cc.setFillWidth(true);
			board.getColumnConstraints().add(cc);

			RowConstraints rc = new RowConstraints();
			rc.setPercentHeight(12.5);
			rc.setVgrow(Priority.ALWAYS);
			rc.setFillHeight(true);
			board.getRowConstraints().add(rc);
		}

		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				Position position = new Position(row, col);
				ChessSquare square = new ChessSquare(position);

				final int r = row, c = col;
				square.setOnMouseClicked(e -> {
					if (squareClickListener != null) {
						squareClickListener.accept(new Position(r, c));
					}
				});

				squares[row][col] = square;
				GridPane.setHgrow(square.getRoot(), Priority.ALWAYS);
				GridPane.setVgrow(square.getRoot(), Priority.ALWAYS);
				board.add(square.getRoot(), col, row);
			}
		}

		boardContainer.getChildren().add(board);

		boardContainer.widthProperty().addListener((obs, o, n) -> resizeBoardToContainer());
		boardContainer.heightProperty().addListener((obs, o, n) -> resizeBoardToContainer());

		applyOrientation();
	}

	private void resizeBoardToContainer() {
		double w = boardContainer.getWidth();
		double h = boardContainer.getHeight();
		if (w <= 0 || h <= 0) {
			return;
		}
		double size = Math.min(w, h);
		board.setPrefSize(size, size);
		board.setMinSize(size, size);
		board.setMaxSize(size, size);
	}

	public void setSquareClickListener(Consumer<Position> listener) {
		this.squareClickListener = listener;
	}

	public void setCurrentBoard(Board board) {
		this.currentBoard = board;
	}

	public void setBottomColor(PieceColor bottomColor) {
		PieceColor effectiveColor = bottomColor != null ? bottomColor : PieceColor.WHITE;
		boolean shouldFlip = effectiveColor == PieceColor.BLACK;
		if (this.isFlipped == shouldFlip) {
			return;
		}
		this.isFlipped = shouldFlip;
		applyOrientation();
	}

	private void applyOrientation() {
		if (squares == null) {
			return;
		}
		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				ChessSquare square = squares[row][col];
				if (square == null) {
					continue;
				}
				int displayRow = mapRow(row);
				int displayCol = mapCol(col);
				GridPane.setRowIndex(square.getRoot(), displayRow);
				GridPane.setColumnIndex(square.getRoot(), displayCol);
			}
		}
	}

	private int mapRow(int logicalRow) {
		return isFlipped ? 7 - logicalRow : logicalRow;
	}

	private int mapCol(int logicalCol) {
		return isFlipped ? 7 - logicalCol : logicalCol;
	}

	public void updateSquare(Position pos, String pieceSymbol, boolean isSelected,
			boolean isPossibleMove, boolean isCaptureMove) {
		if (isValidPosition(pos)) {

			ChessSquare square = squares[pos.getRow()][pos.getCol()];

			String currentSymbol = square.getPieceSymbol();
			if (!currentSymbol.equals(pieceSymbol != null ? pieceSymbol : "")) {
				square.clearHighlight();
			}

			square.updateAppearance(
					pieceSymbol, isSelected, isPossibleMove, isCaptureMove);
		}
	}

	public void updateSquare(Position pos, String pieceSymbol, boolean isSelected, boolean isPossibleMove) {
		updateSquare(pos, pieceSymbol, isSelected, isPossibleMove, false);
	}

	public void clearHighlights() {

		javafx.application.Platform.runLater(() -> {
			for (int row = 0; row < 8; row++) {
				for (int col = 0; col < 8; col++) {
					ChessSquare square = squares[row][col];
					if (square != null) {

						square.getRoot().setEffect(null);
						square.clearHighlight();
					}
				}
			}
		});
	}

	public void highlightMove(Move move) {
		if (move == null)
			return;

		clearHighlightsImmediately();

		ChessSquare fromSquare = getSquare(move.getFrom());
		ChessSquare toSquare = getSquare(move.getTo());

		if (fromSquare != null) {
			fromSquare.highlightAsMovePart();
		}
		if (toSquare != null) {
			toSquare.highlightAsMovePart();
		}
	}

	public void highlightHint(Move move) {
		if (move == null)
			return;

		clearHighlightsImmediately();

		ChessSquare fromSquare = getSquare(move.getFrom());
		ChessSquare toSquare = getSquare(move.getTo());

		if (fromSquare != null) {
			fromSquare.highlightAsHint();
		}
		if (toSquare != null) {
			toSquare.highlightAsHint();
		}
	}

	public void highlightPossibleMoves(Position from, List<Move> possibleMoves) {

		clearHighlightsImmediately();

		if (currentBoard == null) {
			return;
		}

		Piece selectedPiece = currentBoard.getPieceAt(from);
		if (selectedPiece == null) {
			return;
		}

		PauseTransition pause = new PauseTransition(Duration.millis(10));
		pause.setOnFinished(e -> {
			String selectedSymbol = selectedPiece.toUnicode();

			updateSquare(from, selectedSymbol, true, false, false);

			for (Move move : possibleMoves) {
				if (move.getFrom().equals(from)) {
					Position to = move.getTo();
					Piece targetPiece = currentBoard.getPieceAt(to);
					String targetSymbol = targetPiece != null ? targetPiece.toUnicode() : "";
					boolean isCapture = targetPiece != null && targetPiece.getColor() != selectedPiece.getColor();

					updateSquare(to, targetSymbol, false, true, isCapture);
				}
			}
		});
		pause.play();
	}

	private void clearHighlightsImmediately() {
		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				ChessSquare square = squares[row][col];
				if (square != null) {

					square.clearHighlight();
				}
			}
		}
	}

	public void updateBoard(Board board) {
		this.currentBoard = board;
		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				Position pos = new Position(row, col);
				Piece piece = board.getPieceAt(pos);
				String symbol = piece != null ? piece.toUnicode() : "";
				updateSquare(pos, symbol, false, false, false);
			}
		}
	}

	public void updateSingleSquare(Position pos) {
		if (currentBoard != null && isValidPosition(pos)) {
			Piece piece = currentBoard.getPieceAt(pos);
			String symbol = piece != null ? piece.toUnicode() : "";
			squares[pos.getRow()][pos.getCol()].updateAppearance(symbol, false, false, false);
		}
	}

	/**
	 * Método principal para animar movimientos con movimiento fluido
	 */
	public void animateMove(Move move, Runnable onFinished) {
		ChessSquare fromSquare = getSquare(move.getFrom());
		ChessSquare toSquare = getSquare(move.getTo());

		if (fromSquare == null || toSquare == null) {
			if (onFinished != null)
				onFinished.run();
			return;
		}

		String pieceSymbol = "";
		if (!fromSquare.isEmpty()) {
			pieceSymbol = fromSquare.getPieceSymbol();
		}

		boolean isCapture = !toSquare.isEmpty();

		animateSlidingMovement(fromSquare, toSquare, pieceSymbol, isCapture, onFinished);
	}

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

		String pieceSymbol1 = !from1.isEmpty() ? from1.getPieceSymbol() : "";
		String pieceSymbol2 = !from2.isEmpty() ? from2.getPieceSymbol() : "";

		javafx.scene.Node node1 = null;
		javafx.scene.Node node2 = null;

		if (from1.isUsingImages() && from1.getPieceImageView() != null && from1.getPieceImageView().isVisible()) {
			node1 = from1.getPieceImageView();
		} else if (from1.getPieceLabel() != null && from1.getPieceLabel().isVisible()) {
			node1 = from1.getPieceLabel();
		} else {
			node1 = from1.getRoot();
		}

		if (from2.isUsingImages() && from2.getPieceImageView() != null && from2.getPieceImageView().isVisible()) {
			node2 = from2.getPieceImageView();
		} else if (from2.getPieceLabel() != null && from2.getPieceLabel().isVisible()) {
			node2 = from2.getPieceLabel();
		} else {
			node2 = from2.getRoot();
		}

		FadeTransition fadeOut1 = new FadeTransition(Duration.millis(300), node1);
		FadeTransition fadeOut2 = new FadeTransition(Duration.millis(300), node2);

		fadeOut1.setFromValue(1.0);
		fadeOut1.setToValue(0.0);
		fadeOut2.setFromValue(1.0);
		fadeOut2.setToValue(0.0);

		ParallelTransition parallelFadeOut = new ParallelTransition(fadeOut1, fadeOut2);
		parallelFadeOut.setOnFinished(e -> {
			from1.updateAppearance("", false, false, false);
			from2.updateAppearance("", false, false, false);

			to1.updateAppearance(pieceSymbol1, false, false, false);
			to2.updateAppearance(pieceSymbol2, false, false, false);

			final javafx.scene.Node nodeIn1;
			final javafx.scene.Node nodeIn2;

			if (to1.isUsingImages() && to1.getPieceImageView() != null) {
				nodeIn1 = to1.getPieceImageView();
				nodeIn1.setOpacity(0.0);
			} else if (to1.getPieceLabel() != null) {
				nodeIn1 = to1.getPieceLabel();
				nodeIn1.setOpacity(0.0);
			} else {
				nodeIn1 = null;
			}

			if (to2.isUsingImages() && to2.getPieceImageView() != null) {
				nodeIn2 = to2.getPieceImageView();
				nodeIn2.setOpacity(0.0);
			} else if (to2.getPieceLabel() != null) {
				nodeIn2 = to2.getPieceLabel();
				nodeIn2.setOpacity(0.0);
			} else {
				nodeIn2 = null;
			}

			FadeTransition fadeIn1 = null;
			FadeTransition fadeIn2 = null;

			if (nodeIn1 != null) {
				fadeIn1 = new FadeTransition(Duration.millis(300), nodeIn1);
				fadeIn1.setFromValue(0.0);
				fadeIn1.setToValue(1.0);
			}

			if (nodeIn2 != null) {
				fadeIn2 = new FadeTransition(Duration.millis(300), nodeIn2);
				fadeIn2.setFromValue(0.0);
				fadeIn2.setToValue(1.0);
			}

			if (fadeIn1 != null || fadeIn2 != null) {
				ParallelTransition parallelFadeIn = new ParallelTransition();
				if (fadeIn1 != null)
					parallelFadeIn.getChildren().add(fadeIn1);
				if (fadeIn2 != null)
					parallelFadeIn.getChildren().add(fadeIn2);

				parallelFadeIn.setOnFinished(ev -> {
					if (nodeIn1 != null)
						nodeIn1.setOpacity(1.0);
					if (nodeIn2 != null)
						nodeIn2.setOpacity(1.0);

					if (onFinished != null) {
						onFinished.run();
					}
				});
				parallelFadeIn.play();
			} else {
				if (onFinished != null) {
					onFinished.run();
				}
			}
		});

		parallelFadeOut.play();
	}

	/**
	 * Animación de deslizamiento suave
	 */
	private void animateSlidingMovement(ChessSquare fromSquare, ChessSquare toSquare,
			String pieceSymbol, boolean isCapture, Runnable onFinished) {

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
		movingPiece.setEffect(new javafx.scene.effect.DropShadow(10, javafx.scene.paint.Color.BLACK));

		TranslateTransition moveTransition = new TranslateTransition(Duration.millis(400), movingPiece);
		moveTransition.setToX(toCoords[0]);
		moveTransition.setToY(toCoords[1]);
		moveTransition.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);

		javafx.animation.ScaleTransition scaleTransition = new javafx.animation.ScaleTransition(Duration.millis(400),
				movingPiece);
		scaleTransition.setToX(1.0);
		scaleTransition.setToY(1.0);

		ParallelTransition moveAnimation = new ParallelTransition(moveTransition, scaleTransition);

		if (isCapture) {
			animateCaptureEffect(toSquare, () -> {

				executeMoveAnimation(moveAnimation, movingPiece, toSquare, pieceSymbol, onFinished);
			});
		} else {

			executeMoveAnimation(moveAnimation, movingPiece, toSquare, pieceSymbol, onFinished);
		}
	}

	private void executeMoveAnimation(ParallelTransition moveAnimation, javafx.scene.Node movingPiece,
			ChessSquare toSquare, String pieceSymbol, Runnable onFinished) {

		moveAnimation.setOnFinished(e -> {

			boardContainer.getChildren().remove(movingPiece);

			if (onFinished != null) {
				onFinished.run();
			}
		});

		moveAnimation.play();
	}

	private javafx.scene.Node createMovingPiece(ChessSquare fromSquare) {
		double squareSize = Math.min(fromSquare.getRoot().getWidth(), fromSquare.getRoot().getHeight());
		if (squareSize <= 0) {
			squareSize = 60.0;
		}
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

	private void hidePieceInSquare(ChessSquare square) {
		if (square.isUsingImages() && square.getPieceImageView().isVisible()) {
			square.getPieceImageView().setVisible(false);
		} else if (square.getPieceLabel().isVisible()) {
			square.getPieceLabel().setVisible(false);
		}
	}

	private double[] getSquareCoordinates(ChessSquare square) {

		Position pos = square.getPosition();
		double squareSize = Math.min(square.getRoot().getWidth(), square.getRoot().getHeight());
		if (squareSize <= 0) {
			squareSize = 60.0;
		}
		double boardSize = squareSize * 8.0;

		double boardCenterX = boardSize / 2.0;
		double boardCenterY = boardSize / 2.0;

		int displayCol = mapCol(pos.getCol());
		int displayRow = mapRow(pos.getRow());
		double x = (displayCol * squareSize) - boardCenterX + (squareSize / 2);
		double y = (displayRow * squareSize) - boardCenterY + (squareSize / 2);

		return new double[] { x, y };
	}

	/**
	 * Efecto de captura mejorado
	 */
	private void animateCaptureEffect(ChessSquare targetSquare, Runnable onCaptureFinished) {

		final javafx.scene.Node pieceNode;
		if (targetSquare.isUsingImages() && targetSquare.getPieceImageView().isVisible()) {
			pieceNode = targetSquare.getPieceImageView();
		} else if (targetSquare.getPieceLabel().isVisible()) {
			pieceNode = targetSquare.getPieceLabel();
		} else {
			pieceNode = null;
		}

		if (pieceNode == null) {
			onCaptureFinished.run();
			return;
		}

		javafx.animation.ScaleTransition explode = new javafx.animation.ScaleTransition(Duration.millis(200),
				pieceNode);
		explode.setToX(1.3);
		explode.setToY(1.3);
		explode.setInterpolator(javafx.animation.Interpolator.EASE_OUT);

		javafx.animation.RotateTransition rotate = new javafx.animation.RotateTransition(Duration.millis(300),
				pieceNode);
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

			targetSquare.updateAppearance("", false, false, false);

			targetSquare.getRoot().setTranslateX(0);
			targetSquare.getRoot().setTranslateY(0);

			onCaptureFinished.run();
		});

		captureEffect.play();
	}

	private boolean isValidPosition(Position pos) {
		return pos != null &&
				pos.getRow() >= 0 && pos.getRow() < 8 &&
				pos.getCol() >= 0 && pos.getCol() < 8;
	}

	public GridPane getBoard() {
		return board;
	}

	public StackPane getBoardWithAnimations() {
		return boardContainer;
	}

	public ChessSquare getSquare(Position pos) {
		if (isValidPosition(pos)) {
			return squares[pos.getRow()][pos.getCol()];
		}
		return null;
	}
}