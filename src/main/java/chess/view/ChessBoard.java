package chess.view;

import java.util.List;
import java.util.function.Consumer;

import chess.model.Board;
import chess.model.Move;
import chess.model.Piece;
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

	public ChessBoard() {
		initializeBoard();
	}

	private void initializeBoard() {
		boardContainer = new StackPane();
		board = new GridPane();
		squares = new ChessSquare[8][8];

		// Make board responsive - remove fixed size
		board.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		boardContainer.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

		// Ensure grid distributes space evenly (8x8)
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

		// Keep board square based on available space
		boardContainer.widthProperty().addListener((obs, o, n) -> resizeBoardToContainer());
		boardContainer.heightProperty().addListener((obs, o, n) -> resizeBoardToContainer());
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

	public void updateSquare(Position pos, String pieceSymbol, boolean isSelected,
			boolean isPossibleMove, boolean isCaptureMove) {
		if (isValidPosition(pos)) {
			// Obtener la casilla
			ChessSquare square = squares[pos.getRow()][pos.getCol()];

			// Si hay un cambio de pieza, limpiar primero
			String currentSymbol = square.getPieceSymbol();
			if (!currentSymbol.equals(pieceSymbol != null ? pieceSymbol : "")) {
				square.clearHighlight();
			}

			// Actualizar apariencia
			square.updateAppearance(
					pieceSymbol, isSelected, isPossibleMove, isCaptureMove);
		}
	}

	public void updateSquare(Position pos, String pieceSymbol, boolean isSelected, boolean isPossibleMove) {
		updateSquare(pos, pieceSymbol, isSelected, isPossibleMove, false);
	}

	public void clearHighlights() {
		// Usar Platform.runLater para asegurar que se ejecute en el hilo de JavaFX
		javafx.application.Platform.runLater(() -> {
			for (int row = 0; row < 8; row++) {
				for (int col = 0; col < 8; col++) {
					ChessSquare square = squares[row][col];
					if (square != null) {
						// Limpiar inmediatamente, sin animaciones
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

		// Limpiar otros highlights primero
		clearHighlightsImmediately();

		// Resaltar casilla de origen y destino
		ChessSquare fromSquare = getSquare(move.getFrom());
		ChessSquare toSquare = getSquare(move.getTo());

		if (fromSquare != null) {
			fromSquare.highlightAsMovePart();
		}
		if (toSquare != null) {
			toSquare.highlightAsMovePart();
		}
	}

	public void highlightPossibleMoves(Position from, List<Move> possibleMoves) {
		// Limpiar TODOS los highlights primero de forma síncrona
		clearHighlightsImmediately();

		if (currentBoard == null) {
			return;
		}

		Piece selectedPiece = currentBoard.getPieceAt(from);
		if (selectedPiece == null) {
			return;
		}

		// Pequeño delay para asegurar que se limpió
		PauseTransition pause = new PauseTransition(Duration.millis(10));
		pause.setOnFinished(e -> {
			String selectedSymbol = selectedPiece.toUnicode();

			// Actualizar casilla seleccionada
			updateSquare(from, selectedSymbol, true, false, false);

			// Resaltar movimientos posibles
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
					// Llamar al método clearHighlight de la casilla
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

		// Obtener información de la pieza
		String pieceSymbol = "";
		if (!fromSquare.isEmpty()) {
			pieceSymbol = fromSquare.getPieceSymbol();
		}

		boolean isCapture = !toSquare.isEmpty();

		// Usar animación de deslizamiento suave
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

		// Crear una pieza temporal para la animación
		javafx.scene.Node movingPiece = createMovingPiece(fromSquare);
		if (movingPiece == null) {
			if (onFinished != null)
				onFinished.run();
			return;
		}

		// Agregar la pieza temporal al contenedor del tablero
		boardContainer.getChildren().add(movingPiece);

		// Ocultar la pieza original
		hidePieceInSquare(fromSquare);

		// Calcular posición inicial y final
		double[] fromCoords = getSquareCoordinates(fromSquare);
		double[] toCoords = getSquareCoordinates(toSquare);

		// Posicionar la pieza temporal en la posición inicial
		movingPiece.setTranslateX(fromCoords[0]);
		movingPiece.setTranslateY(fromCoords[1]);

		// Efecto de elevación (escala ligeramente)
		movingPiece.setScaleX(1.1);
		movingPiece.setScaleY(1.1);
		movingPiece.setEffect(new javafx.scene.effect.DropShadow(10, javafx.scene.paint.Color.BLACK));

		// Animación de movimiento
		TranslateTransition moveTransition = new TranslateTransition(Duration.millis(400), movingPiece);
		moveTransition.setToX(toCoords[0]);
		moveTransition.setToY(toCoords[1]);
		moveTransition.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);

		// Animación de aterrizaje (volver a escala normal)
		javafx.animation.ScaleTransition scaleTransition = new javafx.animation.ScaleTransition(Duration.millis(400),
				movingPiece);
		scaleTransition.setToX(1.0);
		scaleTransition.setToY(1.0);

		// Ejecutar animaciones en paralelo
		ParallelTransition moveAnimation = new ParallelTransition(moveTransition, scaleTransition);

		// Si es captura, animar efecto de captura primero
		if (isCapture) {
			animateCaptureEffect(toSquare, () -> {
				// Después de la captura, ejecutar el movimiento
				executeMoveAnimation(moveAnimation, movingPiece, toSquare, pieceSymbol, onFinished);
			});
		} else {
			// Ejecutar movimiento directamente
			executeMoveAnimation(moveAnimation, movingPiece, toSquare, pieceSymbol, onFinished);
		}
	}

	private void executeMoveAnimation(ParallelTransition moveAnimation, javafx.scene.Node movingPiece,
			ChessSquare toSquare, String pieceSymbol, Runnable onFinished) {

		moveAnimation.setOnFinished(e -> {
			// Remover la pieza temporal
			boardContainer.getChildren().remove(movingPiece);

			// Importante: NO pintar la pieza aquí.
			// El controlador vuelve a repintar 'from' y 'to' después de applyMove();
			// si pintamos aquí también, se producen flashes/parpadeos por doble actualización.
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
			// Crear una copia del ImageView
			javafx.scene.image.ImageView copy = new javafx.scene.image.ImageView(
					fromSquare.getPieceImageView().getImage());
			copy.setFitWidth(squareSize * 0.82);
			copy.setFitHeight(squareSize * 0.82);
			copy.setPreserveRatio(true);
			return copy;
		} else if (fromSquare.getPieceLabel().isVisible()) {
			// Crear una copia del Label
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
		// Calcular la posición relativa de la casilla dentro del tablero
		Position pos = square.getPosition();
		double squareSize = Math.min(square.getRoot().getWidth(), square.getRoot().getHeight());
		if (squareSize <= 0) {
			squareSize = 60.0;
		}
		double boardSize = squareSize * 8.0;

		// Calcular offset desde el centro del tablero (StackPane centra los hijos)
		double boardCenterX = boardSize / 2.0;
		double boardCenterY = boardSize / 2.0;

		double x = (pos.getCol() * squareSize) - boardCenterX + (squareSize / 2);
		double y = (pos.getRow() * squareSize) - boardCenterY + (squareSize / 2);

		return new double[] { x, y };
	}

	/**
	 * Efecto de captura mejorado
	 */
	private void animateCaptureEffect(ChessSquare targetSquare, Runnable onCaptureFinished) {
		// Obtener el nodo de la pieza a capturar
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

		// Efecto de explosión: escala rápida hacia arriba y luego desvanecimiento
		javafx.animation.ScaleTransition explode = new javafx.animation.ScaleTransition(Duration.millis(200),
				pieceNode);
		explode.setToX(1.3);
		explode.setToY(1.3);
		explode.setInterpolator(javafx.animation.Interpolator.EASE_OUT);

		// Rotación para efecto dramático
		javafx.animation.RotateTransition rotate = new javafx.animation.RotateTransition(Duration.millis(300),
				pieceNode);
		rotate.setByAngle(360);

		// Desvanecimiento
		FadeTransition fadeOut = new FadeTransition(Duration.millis(250), pieceNode);
		fadeOut.setDelay(Duration.millis(100)); // Pequeño delay para que se vea la explosión
		fadeOut.setFromValue(1.0);
		fadeOut.setToValue(0.0);

		// Efecto de sacudida en la casilla
		TranslateTransition shake = new TranslateTransition(Duration.millis(100), targetSquare.getRoot());
		shake.setByX(3);
		shake.setCycleCount(6);
		shake.setAutoReverse(true);

		// Ejecutar efectos en paralelo
		ParallelTransition captureEffect = new ParallelTransition(explode, rotate, fadeOut, shake);
		captureEffect.setOnFinished(e -> {
			// Restaurar estado normal del nodo ANTES de limpiar la casilla.
			// Si limpiamos primero, updateAppearance puede ocultar/cambiar el nodo y causar parpadeo.
			pieceNode.setScaleX(1.0);
			pieceNode.setScaleY(1.0);
			pieceNode.setRotate(0);
			pieceNode.setOpacity(1.0);

			// Limpiar la pieza capturada
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